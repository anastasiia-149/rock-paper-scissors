package com.techub.rps.boundary.incoming;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Game API Integration Tests")
class GameApiControllerIntegrationTest {

    private static final String GAME_API_PATH = "/api/v1/game/play";
    private static final String[] VALID_HANDS = {"ROCK", "PAPER", "SCISSORS"};
    private static final String[] VALID_RESULTS = {"WIN", "LOSE", "DRAW"};
    private static final String TIMESTAMP_PATTERN = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    @DisplayName("POST /play with ROCK should return valid game response")
    void playGame_withRock_shouldReturnValidResponse() {
        given()
                .contentType(ContentType.JSON)
                .body(createBody("ROCK"))
                .when()
                .post(GAME_API_PATH)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("gameId", notNullValue())
                .body("playerHand", equalTo("ROCK"))
                .body("computerHand", in(VALID_HANDS))
                .body("result", in(VALID_RESULTS))
                .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("POST /play with PAPER should return valid game response")
    void playGame_withPaper_shouldReturnValidResponse() {
        given()
                .contentType(ContentType.JSON)
                .body(createBody("PAPER"))
                .when()
                .post(GAME_API_PATH)
                .then()
                .statusCode(200)
                .body("gameId", notNullValue())
                .body("playerHand", equalTo("PAPER"))
                .body("computerHand", in(VALID_HANDS))
                .body("result", in(VALID_RESULTS));
    }

    @Test
    @DisplayName("POST /play with SCISSORS should return valid game response")
    void playGame_withScissors_shouldReturnValidResponse() {
        given()
                .contentType(ContentType.JSON)
                .body(createBody("SCISSORS"))
                .when()
                .post(GAME_API_PATH)
                .then()
                .statusCode(200)
                .body("gameId", notNullValue())
                .body("playerHand", equalTo("SCISSORS"))
                .body("computerHand", in(VALID_HANDS))
                .body("result", in(VALID_RESULTS));
    }

    @Test
    @DisplayName("POST /play with invalid hand should return 400")
    void playGame_withInvalidHand_shouldReturn400() {
        given()
                .contentType(ContentType.JSON)
                .body(createBody("INVALID_HAND"))
                .when()
                .post(GAME_API_PATH)
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", containsString("Invalid request body"))
                .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("POST /play with empty hand should return 400")
    void playGame_withNullHand_shouldReturn400() {
        given()
                .contentType(ContentType.JSON)
                .body(createBody(""))
                .when()
                .post(GAME_API_PATH)
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", notNullValue())
                .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("POST /play with missing request body should return 400")
    void playGame_withMissingBody_shouldReturn400() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post(GAME_API_PATH)
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("POST /play with empty body should return 400")
    void playGame_withEmptyBody_shouldReturn400() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post(GAME_API_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /play with invalid content type should return 415")
    void playGame_withInvalidContentType_shouldReturn415() {
        given()
                .contentType(ContentType.TEXT)
                .body("ROCK")
                .when()
                .post(GAME_API_PATH)
                .then()
                .statusCode(415);
    }

    @Test
    @DisplayName("GET /play should return 405 Method Not Allowed")
    void playGame_withGetMethod_shouldReturn405() {
        given()
                .when()
                .get(GAME_API_PATH)
                .then()
                .statusCode(405)
                .body("status", equalTo(405))
                .body("message", containsString("not supported"));
    }


    @Test
    @DisplayName("Response should contain valid timestamp in ISO format")
    void playGame_shouldReturnValidTimestamp() {
        given()
                .contentType(ContentType.JSON)
                .body(createBody("ROCK"))
                .when()
                .post(GAME_API_PATH)
                .then()
                .statusCode(200)
                .body("timestamp", matchesPattern(TIMESTAMP_PATTERN));
    }

    @Test
    @DisplayName("Each game should generate unique game ID")
    void playGame_shouldGenerateUniqueGameIds() {
        String gameId1 = given()
                .contentType(ContentType.JSON)
                .body(createBody("ROCK"))
                .when()
                .post(GAME_API_PATH)
                .then()
                .statusCode(200)
                .extract()
                .path("gameId");

        String gameId2 = given()
                .contentType(ContentType.JSON)
                .body(createBody("ROCK"))
                .when()
                .post(GAME_API_PATH)
                .then()
                .statusCode(200)
                .extract()
                .path("gameId");

        assertThat(gameId1).isNotEqualTo(gameId2);
    }

    private String createBody(String value) {
        return String.format("""
                {
                  "playerHand": "%s"
                }
                """, value);
    }
}
