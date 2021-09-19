package fr.hardcoding.twitter.raffle.service;

import fr.hardcoding.twitter.raffle.api.model.Winner;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import twitter4j.TwitterException;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class tests the {@link RaffleService}.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
@QuarkusTest
class RaffleServiceTest {
    /**
     * Inject a deterministic raffle service.
     */
    @Inject
    RaffleService raffleService;

    /**
     * Run a huge number of raffle and ensure any participant have chance to win.
     *
     * @throws TwitterException If the winner cannot be retrieved.
     */
    @Test
    void testWinningChance() throws TwitterException {
        Map<String, Integer> winnerCount = new HashMap<>();
        for (int i = 0; i < 10_000; i++) {
            List<Winner> winners = this.raffleService.performRaffle("@someone");
            String winnerName = winners.get(0).screenName;
            winnerCount.compute(winnerName, (name, count) -> count == null ? 1 : count + 1);
        }
        assertEquals(100, winnerCount.size(), "Some participants did not win.");
        assertThat("Some participants have a too few chance to win.", winnerCount.values(), everyItem(greaterThan(50)));
        assertThat("Some participants have a too high chance to win.", winnerCount.values(), everyItem(lessThan(150)));
    }
}
