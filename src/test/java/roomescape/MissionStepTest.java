package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MissionStepTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("시간 저장 및 삭제")
    void saveAndDeleteTime() {
        final Map<String, String> params = Map.of("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(8));

        RestAssured.given().log().all()
                .when().delete("/times/8")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().delete("/times/8")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("테마 저장 및 삭제")
    void saveAndDeleteTheme() {
        final Map<String, String> params = Map.of("name", "v1", "description", "blah", "thumbnail", "dkdk");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(9));

        RestAssured.given().log().all()
                .when().delete("/themes/9")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().delete("/themes/9")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 저장 및 삭제")
    void saveAndDeleteReservation() {
        final Map<String, String> params = Map.of("name", "redddy", "date", "2024-06-13", "timeId", "1", "themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(9));

        RestAssured.given().log().all()
                .when().delete("/reservations/9")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().delete("/reservations/9")
                .then().log().all()
                .statusCode(400);
    }
}
