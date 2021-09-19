package fr.hardcoding.twitter.raffle.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;

/**
 * This class tests the {@link RaffleApi}.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
@QuarkusTest
public class RaffleApiTest {
    /**
     * Test default scenario: create a raffle then retrieves it.
     */
    @Test
    public void createRaffle() {
        String raffleId = given()
                .contentType(JSON)
                .body("@someone")
                .when().post("/api/raffle")
                .then()
                .statusCode(201)
                .contentType(JSON)
                .body("id", not(""),
                        "query", is("@someone"),
                        "winners", hasSize(10)
                )
                .extract().path("id");

        given()
                .pathParam("id", raffleId)
                .when().get("/api/raffle/{id}")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("id", is(raffleId));
    }

    /**
     * Test invalid creation scenario.
     */
    @Test
    public void createInvalidRaffle() {
        given()
                .when().post("/api/raffle")
                .then()
                .statusCode(415);

        given()
                .contentType(JSON)
                .when().post("/api/raffle")
                .then()
                .statusCode(400);

        given()
                .body("  ")
                .contentType(JSON)
                .when().post("/api/raffle")
                .then()
                .statusCode(400);
    }

    /**
     * Test invalid retrieve scenario.
     */
    @Test
    public void getInvalidRaffle() {
        given()
                .pathParam("id", "invalid-id")
                .when().get("/api/raffle/{id}")
                .then()
                .statusCode(404);
    }
}
