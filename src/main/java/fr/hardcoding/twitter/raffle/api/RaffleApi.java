package fr.hardcoding.twitter.raffle.api;

import fr.hardcoding.twitter.raffle.api.model.Raffle;
import fr.hardcoding.twitter.raffle.service.RaffleService;
import twitter4j.TwitterException;

import javax.inject.Inject;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

/**
 * This API provides the Twitter raffle resource.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
@Path("/api/raffle")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class RaffleApi {
    private static final Logger LOGGER = Logger.getLogger(RaffleApi.class.getName());
    private final Map<String, Raffle> raffles;

    @Inject
    RaffleService raffleService;

    /**
     * Public constructor.
     */
    public RaffleApi() {
        this.raffles = new HashMap<>();
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
            Raffle raffle = new Raffle(UUID.randomUUID().toString(), speaker, this.raffleService.performRaffle(speaker));
            this.raffles.put(raffle.id, raffle);
            URI raffleLocation = uriInfo.getRequestUriBuilder().path(raffle.id).build();
            return Response.created(raffleLocation).entity(raffle).build();
        } catch (TwitterException exception) {
            LOGGER.log(SEVERE, "Failed to query Twitter API.", exception);
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
}
