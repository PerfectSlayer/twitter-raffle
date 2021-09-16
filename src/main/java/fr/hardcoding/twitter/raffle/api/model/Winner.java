package fr.hardcoding.twitter.raffle.api.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import twitter4j.Status;

/**
 * This class represents a raffle winner and its tweet information.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
@RegisterForReflection
public class Winner {
    public String name;
    public String screenName;
    public String tweetId;

    public static Winner fromStatus(Status status) {
        Winner winner = new Winner();
        winner.name = status.getUser().getName();
        winner.screenName = status.getUser().getScreenName();
        winner.tweetId = Long.toString(status.getId());
        return winner;
    }

    public String getTweetUrl() {
        return "https://twitter.com/" + this.screenName + "/status/" + this.tweetId;
    }

    public String toString() {
        return this.name + " (@" + this.screenName + "): " + getTweetUrl();
    }
}
