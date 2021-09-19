package fr.hardcoding.twitter.raffle.service;

import io.quarkus.test.Mock;
import twitter4j.Query;
import twitter4j.Status;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class mocks the {@link RaffleService} by returning a deterministic set of participants.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
@Mock
@ApplicationScoped
public class MockRaffleService extends RaffleService {
    private final Map<String, List<Status>> results;

    public MockRaffleService() {
        this.results = new HashMap<>();
        for (int id = 0; id < MAX_RESULT; id++) {
            Status status = mockStatus(id);
            String name = status.getUser().getName();
            this.results.put(name, List.of(status));
        }
    }

    private Status mockStatus(long id) {
        Status status = mock(Status.class, RETURNS_DEEP_STUBS);
        when(status.getId()).thenReturn(id);
        when(status.getUser().getName()).thenReturn("User " + id);
        when(status.getUser().getScreenName()).thenReturn("User" + id);
        return status;
    }

    @Override
    protected Map<String, List<Status>> performQuery(Query query, Predicate<Status> filter) {
        return this.results;
    }
}
