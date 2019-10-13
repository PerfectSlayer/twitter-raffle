package fr.hardcoding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

@Path("/")
public class TwitterRaffle {
    private static final Logger LOGGER = Logger.getLogger(TwitterRaffle.class.getName());
    private static final int WINER_COUNT = 10;
    private Twitter twitter = new TwitterFactory().getInstance();

    @Path("/raffle")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Winner> hello(@QueryParam("speaker") String speaker) {
        return performRaffle(speaker);
    }

    private List<Winner> performRaffle(String screenName) {
        Map<String, List<Status>> userTweets;
        try {
            Query query = getQuery(screenName);
            userTweets = performQuery(query);
        } catch (TwitterException exception) {
            LOGGER.log(Level.SEVERE, "Failed to query twitter.", exception);
            return Collections.emptyList();
        }

        Random rand = new Random(System.currentTimeMillis());
        List<String> users = new LinkedList<>(userTweets.keySet());
        Set<String> winningUsers = new HashSet<>();

        while (winningUsers.size() < Math.min(WINER_COUNT, userTweets.size())) {
            String winner = users.remove(rand.nextInt(users.size()));
            winningUsers.add(winner);
        }

        List<Winner> winners = winningUsers.stream()
                .map(userTweets::get)
                .map(list -> list.get(0))
                .map(Winner::fromStatus)
                .collect(Collectors.toList());

        LOGGER.fine(winners.get(0).toString());

        return winners;
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
            result = twitter.search(query);
            // RateLimitStatus limit = result.getRateLimitStatus();
            // log.info(String.format("Rate Limit for Query: %s/%s. Reset in %s seconds",
            // limit.getRemaining(), limit.getLimit(), limit.getSecondsUntilReset()));
            List<Status> tweets = result.getTweets();
            for (Status tweet : tweets) {
                String screenName = tweet.getUser().getScreenName();
                userTweets.computeIfAbsent(screenName, __ -> new ArrayList<>()).add(tweet);
            }
        } while ((query = result.nextQuery()) != null);
        return userTweets;
    }
}