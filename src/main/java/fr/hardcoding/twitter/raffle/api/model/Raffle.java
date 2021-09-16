package fr.hardcoding.twitter.raffle.api.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * This class represents a raffle and its winner.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
@RegisterForReflection
public class Raffle {
    public final String id;
    public final String query;
    public final List<Winner> winners;

    public Raffle(String id, String speaker, List<Winner> winners) {
        this.id = id;
        this.query = speaker;
        this.winners = winners;
    }

    @Override
    public String toString() {
        return "Raffle" + this.id + "(" + this.query + ").";
    }
}
