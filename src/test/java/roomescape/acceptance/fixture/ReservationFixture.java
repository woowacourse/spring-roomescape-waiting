package roomescape.acceptance.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;

public class ReservationFixture {

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