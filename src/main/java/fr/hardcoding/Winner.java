package fr.hardcoding;

import twitter4j.Status;

public class Winner {
    public String name;
    public String screenName;
    public String tweetUrl;

    public static Winner fromStatus(Status status) {
        Winner winner = new Winner();
        winner.name = status.getUser().getName();
        winner.screenName = status.getUser().getScreenName();
        winner.tweetUrl = "https://twitter.com/"+winner.screenName+"/status/"+status.getId();
        return winner;
    }

    public String toString() {
        return this.name+" (@"+this.screenName+"): "+this.tweetUrl;
    }
}