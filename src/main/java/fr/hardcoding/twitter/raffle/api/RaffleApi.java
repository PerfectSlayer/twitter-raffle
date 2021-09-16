package fr.hardcoding.twitter.raffle.api;

import fr.hardcoding.twitter.raffle.api.model.Raffle;
import fr.hardcoding.twitter.raffle.api.model.Winner;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

/**
 * This API provides the Twitter raffle service.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
@Path("/api/raffle")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class RaffleApi {
    private static final String TWITTER4J_OAUTH_CONSUMER_KEY = "twitter4j_oauth_consumerKey";
    private static final String TWITTER4J_OAUTH_CONSUMER_SECRET = "twitter4j_oauth_consumerSecret";
    private static final String TWITTER4J_OAUTH_ACCESS_TOKEN = "twitter4j_oauth_accessToken";
    private static final String TWITTER4J_OAUTH_ACCESS_TOKEN_SECRET = "twitter4j_oauth_accessTokenSecret";
    private static final Logger LOGGER = Logger.getLogger(RaffleApi.class.getName());
    private static final int WINNER_COUNT = 10;
    private static final int MAX_RESULT = 100;
    private final Twitter twitter;
    private final Map<String, Raffle> raffles;

    /**
     * Public constructor.
     */
    public RaffleApi() {
        this.twitter = getTwitterClient();
        this.raffles = new HashMap<>();
    }

    /**
     * Initialize Twitter client with credentials from environment variables.
     *
     * @return A Twitter client.
     */
    private static Twitter getTwitterClient() {
        if (System.getenv(TWITTER4J_OAUTH_CONSUMER_KEY) != null
                && System.getenv(TWITTER4J_OAUTH_CONSUMER_SECRET) != null
                && System.getenv(TWITTER4J_OAUTH_ACCESS_TOKEN) != null
                && System.getenv(TWITTER4J_OAUTH_ACCESS_TOKEN_SECRET) != null) {
            LOGGER.info("Initializing twitter client with credentials.");

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setOAuthConsumerKey(System.getenv(TWITTER4J_OAUTH_CONSUMER_KEY))
                    .setOAuthConsumerSecret(System.getenv(TWITTER4J_OAUTH_CONSUMER_SECRET))
                    .setOAuthAccessToken(System.getenv(TWITTER4J_OAUTH_ACCESS_TOKEN))
                    .setOAuthAccessTokenSecret(System.getenv(TWITTER4J_OAUTH_ACCESS_TOKEN_SECRET));
            TwitterFactory tf = new TwitterFactory(cb.build());

            return tf.getInstance();
        } else {
            LOGGER.warning("Initializing Twitter client without credentials.");
            return new TwitterFactory().getInstance();
        }
    }

    /**
     * Create a raffle.
     *
     * @param speaker The speaker to
     * @param uriInfo The URI information context.
     * @return The raffle creation status and location.
     */
    @Path("/")
    @POST
    public Response createRaffle(String speaker, @Context UriInfo uriInfo) {
        // Check speaker
        if (speaker == null || speaker.isBlank()) {
            return Response.status(BAD_REQUEST).build();
        }
        // Create raffle
        try {
            Raffle raffle = new Raffle(UUID.randomUUID().toString(), speaker, performRaffle(speaker));
            this.raffles.put(raffle.id, raffle);
            URI raffleLocation = uriInfo.getRequestUriBuilder().path(raffle.id).build();
            return Response.created(raffleLocation).entity(raffle).build();
        } catch (TwitterException exception) {
            LOGGER.log(SEVERE, "Failed to query tweets.", exception);
            return Response.status(SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Get a raffle result.
     *
     * @param raffleId The identifier of the raffle.
     * @return The raffle result or not-found if the raffle does not exist.
     */
    @Path("/{raffleId}")
    @GET
    public Response getRaffle(@PathParam("raffleId") String raffleId) {
        Raffle raffle = this.raffles.get(raffleId);
        if (raffle == null) {
            LOGGER.log(WARNING, "Failed to find raffle {}.", raffleId);
            return Response.status(NOT_FOUND).build();
        }
        return Response.ok(raffle).build();
    }

    private List<Winner> performRaffle(String speaker) throws TwitterException {
        Query query = getQuery(speaker);
        Predicate<Status> filter = getTweetFilter(speaker);
        Map<String, List<Status>> userTweets = performQuery(query, filter);

        Random rand = new Random(System.currentTimeMillis());
        List<String> users = new LinkedList<>(userTweets.keySet());
        Set<String> winningUsers = new HashSet<>();

        while (winningUsers.size() < Math.min(WINNER_COUNT, userTweets.size())) {
            String winner = users.remove(rand.nextInt(users.size()));
            winningUsers.add(winner);
        }

        return winningUsers.stream()
                .map(userTweets::get)
                .map(list -> list.get(0))
                .map(Winner::fromStatus)
                .collect(Collectors.toList());
    }

    private Query getQuery(String speaker) {
        // - Must mention ParisJUG
        // - Must mention the given speaker
        // - Must include a picture and must
        // - Must NOT be a RT
        String queryString = "parisjug " + speaker + " filter:media -filter:retweets";
        return new Query(queryString);
    }

    private Predicate<Status> getTweetFilter(String speaker) {
        // Remove parisjug, given speaker, white spaces, twitter URL and check if remains few text
        return status -> {
            String text = status.getText();
            String originalText = text;
            text = text.replace("parisjug", "");
            for (String part : speaker.split(" ")) {
                text = text.replace(part, "");
            }
            text = text.replaceAll("https://t\\.co/[a-zA-Z0-9]+", "");
            text = text.replaceAll("[ \n]", "");
            if (text.length() <= 5) {
                LOGGER.info("Filtering tweet: " + originalText);
                return false;
            }
            return true;
        };
    }

    private Map<String, List<Status>> performQuery(Query query, Predicate<Status> filter) throws TwitterException {
        // Map of found tweeds indexed by user screen names
        Map<String, List<Status>> userTweets = new HashMap<>();
        QueryResult result;
        do {
            result = twitter.search(query);
            RateLimitStatus limit = result.getRateLimitStatus();
            LOGGER.info(String.format(
                    "Rate Limit for Query: %s/%s. Reset in %s seconds",
                    limit.getRemaining(),
                    limit.getLimit(),
                    limit.getSecondsUntilReset()
            ));
            List<Status> tweets = result.getTweets();
            for (Status tweet : tweets) {
                if (filter.test(tweet)) {
                    String screenName = tweet.getUser().getScreenName();
                    userTweets.computeIfAbsent(screenName, __ -> new ArrayList<>()).add(tweet);
                }
            }
        } while ((query = result.nextQuery()) != null && userTweets.size() < MAX_RESULT);
        return userTweets;
    }
}
