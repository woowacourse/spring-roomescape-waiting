package roomescape.acceptance.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import roomescape.domain.ReservationStatus;

public class ReservationFixture {

    public static void createReservation(String name, String date, Long timeId, Long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        var response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract();

        String status = response.path("status");
        if (ReservationStatus.PENDING.name().equals(status)) {
            confirmPayment(response.path("orderId"), response.path("amount"));
        }
    }

    public static Map<String, Object> createPendingReservation(String name, String date, Long timeId, Long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .as(Map.class);
    }

    public static void confirmPayment(String orderId, Integer amount) {
        Map<String, Object> params = new HashMap<>();
        params.put("paymentKey", "test_payment_key_" + orderId);
        params.put("orderId", orderId);
        params.put("amount", amount.longValue());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/payments/confirm")
                .then().log().all()
                .statusCode(200);
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
