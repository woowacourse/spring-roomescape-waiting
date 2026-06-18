package roomescape.acceptance.step;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import roomescape.domain.ReservationStatus;

public class ReservationSteps {

    public static void saveReservation(Long memberId, String date, Long timeId, Long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .header("Member-Id", memberId)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    public static void findByName(String name, int expectedSize, ReservationStatus reservationStatus) {
        String key = "reservations";
        if (reservationStatus == ReservationStatus.WAITING) {
            key = "waits";
        }
        RestAssured.given().log().all()
                .queryParam("name", name)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body(key + ".items.size()", is(expectedSize))
                .body(key + ".items[0].status", is(reservationStatus.name()));
    }

    public static void checkAllReservationSize(int reservationSize, int waitSize) {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.items.size()", is(reservationSize))
                .body("waits.items.size()", is(waitSize));
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
