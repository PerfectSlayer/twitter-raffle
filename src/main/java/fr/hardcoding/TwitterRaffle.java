package fr.hardcoding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

@Path("/")
public class TwitterRaffle {
    private static final int WINER_COUNT = 10;
    private Twitter twitter = new TwitterFactory().getInstance();

    @Path("/raffle")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Status> hello(@QueryParam("user") String screenName) {
        return performRaffle(screenName);
    }

    private List<Status> performRaffle(String screenName) {
        Map<String, List<Status>> userTweets;
        try {
            Query query = getQuery(screenName);
            userTweets = performQuery(query);
        } catch (TwitterException e) {
            // TODO LOG
            return Collections.emptyList();
        }

        Random rand = new Random(System.currentTimeMillis());
        List<String> users = new LinkedList<>(userTweets.keySet());
        Set<String> winners = new HashSet<>();

        while (winners.size() < Math.min(WINER_COUNT, userTweets.size())) {
            String winner = users.remove(rand.nextInt(users.size()));
            winners.add(winner);
        }

        List<Status> winningTweets = winners.stream().map(userTweets::get).map(list -> list.get(0))
                .collect(Collectors.toList());

        return winningTweets;
    }

    private Query getQuery(String screenName) {
        // Must mention ParisJUG plus a custom screenName, include a picture and must
        // not be a RT
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