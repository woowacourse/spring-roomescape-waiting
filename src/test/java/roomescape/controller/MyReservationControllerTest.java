package roomescape.controller;

import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MyReservationControllerTest extends ControllerTest {

    @DisplayName("내 예약 목록 — CONFIRMED만 있는 경우")
    @Test
    void 내_예약_목록_CONFIRMED만() {
        RestAssured.given().log().all()
                .queryParam("username", "김철수")
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations", hasSize(4));
    }

    @DisplayName("내 예약 목록 — CONFIRMED + WAITING 합산")
    @Test
    void 내_예약_목록_CONFIRMED_와_WAITING_합산() {
        String futureDate = LocalDate.now().plusDays(5).toString();

        Map<String, Object> reservationParams = new HashMap<>();
        reservationParams.put("name", "김철수");
        reservationParams.put("date", futureDate);
        reservationParams.put("timeId", 8);
        reservationParams.put("themeId", 4);
        RestAssured.given().contentType(ContentType.JSON).body(reservationParams)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> waitingParams = new HashMap<>();
        waitingParams.put("name", "이영희");
        waitingParams.put("date", futureDate);
        waitingParams.put("timeId", 8);
        waitingParams.put("themeId", 4);
        RestAssured.given().contentType(ContentType.JSON).body(waitingParams)
                .when().post("/reservations/waiting").then().statusCode(201);

        RestAssured.given().log().all()
                .queryParam("username", "이영희")
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations", hasSize(4));
    }

    @DisplayName("내 예약 목록 — 예약 없는 사용자는 빈 목록")
    @Test
    void 내_예약_목록_없으면_빈_목록() {
        RestAssured.given().log().all()
                .queryParam("username", "없는사람")
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations", hasSize(0));
    }
}
