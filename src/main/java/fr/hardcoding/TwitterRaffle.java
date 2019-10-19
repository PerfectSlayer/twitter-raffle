package fr.hardcoding;


import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

@Path("/")
public class TwitterRaffle {
    private static final Logger LOGGER = Logger.getLogger(TwitterRaffle.class.getName());
    private static final int WINNER_COUNT = 10;
    private static final int MAX_RESULT = 100;
    private Twitter twitter = new TwitterFactory().getInstance();

    @Path("/raffle")
    @GET
    @Produces(APPLICATION_JSON)
    public Response hello(@QueryParam("speaker") String speaker) {
        try {
            List<Winner> winners = performRaffle(speaker);
            return Response.ok(winners).build();
        } catch (TwitterException exception) {
            LOGGER.log(Level.SEVERE, "Failed to query tweets", exception);
            return Response.status(SERVICE_UNAVAILABLE).build();
        }
    }

    private List<Winner> performRaffle(String screenName) throws TwitterException {
        Map<String, List<Status>> userTweets;
        Query query = getQuery(screenName);
        userTweets = performQuery(query);

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

    private Query getQuery(String screenName) {
        // - Must mention ParisJUG
        // - Must mention the given screenName
        // - Must include a picture and must
        // - Must NOT be a RT
        String queryString = "@parisjug @" + screenName + " filter:media -filter:retweets";
        return new Query(queryString);
    }

    private Map<String, List<Status>> performQuery(Query query) throws TwitterException {
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
                String screenName = tweet.getUser().getScreenName();
                userTweets.computeIfAbsent(screenName, __ -> new ArrayList<>()).add(tweet);
            }
        } while ((query = result.nextQuery()) != null && userTweets.size() < MAX_RESULT);
        return userTweets;
    }
}