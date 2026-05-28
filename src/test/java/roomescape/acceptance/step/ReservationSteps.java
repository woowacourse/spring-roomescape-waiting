package roomescape.acceptance.step;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;

public class ReservationSteps {

    public static void createReservation(String name, String date, Long timeId, Long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    public static void readMyName(String name, int expectedSize, String reservationStatus) {
        RestAssured.given().log().all()
                .queryParam("name", name)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(expectedSize))
                .body("[0].status", is(reservationStatus));
    }

    public static void checkAllReservationSize(int expectedSize) {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(expectedSize));
    }

    public static void deleteReservation(Long id) {
        RestAssured.given().log().all()
                .when().delete("/reservations/" + id)
                .then().log().all()
                .statusCode(204);
    }

    public static void deleteWait(Long id) {
        RestAssured.given().log().all()
                .when().delete("/reservations/waits/" + id)
                .then().log().all()
                .statusCode(204);
    }
}
