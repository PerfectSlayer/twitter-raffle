package fr.hardcoding.twitter.raffle.service;

import fr.hardcoding.twitter.raffle.api.model.Winner;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.Math.min;

/**
 * This service perform the Twitter raffle.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
@Singleton
public class RaffleService {
    // Twitter4j configuration
    private static final Logger LOGGER = Logger.getLogger(RaffleService.class.getName());
    private static final String TWITTER4J_OAUTH_CONSUMER_KEY = "twitter4j_oauth_consumerKey";
    private static final String TWITTER4J_OAUTH_CONSUMER_SECRET = "twitter4j_oauth_consumerSecret";
    private static final String TWITTER4J_OAUTH_ACCESS_TOKEN = "twitter4j_oauth_accessToken";
    private static final String TWITTER4J_OAUTH_ACCESS_TOKEN_SECRET = "twitter4j_oauth_accessTokenSecret";
    // Raffle configuration
    protected static final int WINNER_COUNT = 10;
    protected static final int MAX_RESULT = 100;
    // Twitter client
    private final Twitter twitter;

    @ConfigProperty(name = "raffle.excluded")
    Set<String> excludedAccounts;

    /**
     * Public constructor.
     */
    public RaffleService() {
        this.twitter = getTwitterClient();
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
     * Perform the raffle.
     *
     * @param speaker The speaker to quote to win.
     * @return The list of raffle winners.
     * @throws TwitterException if the winners can't be retrieved.
     */
    public List<Winner> performRaffle(String speaker) throws TwitterException {
        Query query = getQuery(speaker);
        Predicate<Status> filter = getTweetFilter(speaker);
        Map<String, List<Status>> userTweets = performQuery(query, filter);

        Random rand = new Random(System.currentTimeMillis());
        List<String> users = new LinkedList<>(userTweets.keySet());
        List<String> winningUsers = new LinkedList<>();

        while (winningUsers.size() < min(WINNER_COUNT, userTweets.size())) {
            String winner = users.remove(rand.nextInt(users.size()));
            if (this.excludedAccounts.contains(winner)) {
                continue;
            }
            winningUsers.add(winner);
        }

        return winningUsers.stream()
                .map(userTweets::get)
                .map(list -> list.get(0))
                .map(Winner::fromStatus)
                .collect(Collectors.toList());
    }

    protected Query getQuery(String speaker) {
        // - Must mention ParisJUG
        // - Must mention the given speaker
        // - Must include a picture and must
        // - Must NOT be a RT
        String queryString = "parisjug " + speaker + " filter:media -filter:retweets";
        return new Query(queryString);
    }

    protected Predicate<Status> getTweetFilter(String speaker) {
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

    protected Map<String, List<Status>> performQuery(Query query, Predicate<Status> filter) throws TwitterException {
        // Map of found tweeds indexed by user screen names
        Map<String, List<Status>> userTweets = new HashMap<>();
        QueryResult result;
        do {
            result = this.twitter.search(query);
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
