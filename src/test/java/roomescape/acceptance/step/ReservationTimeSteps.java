package roomescape.acceptance.step;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;

public class ReservationTimeSteps {

    public static void createReservationTime(String startAt) {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", startAt);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    public static void checkAllReservationTimeSize(int expectedSize) {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(expectedSize));
    }

    public static void deleteReservationTime(Long id) {
        RestAssured.given().log().all()
                .when().delete("/times/" + id)
                .then().log().all()
                .statusCode(204);
    }

    public static void checkAvailableReservation(String date, Long themeId, boolean expectedAvailable) {
        RestAssured.given().log().all()
                .queryParam("date", date)
                .queryParam("themeId", themeId)
                .when().get("/times/available")
                .then().log().all()
                .statusCode(200)
                .body("[0].available", is(expectedAvailable));
    }
}
