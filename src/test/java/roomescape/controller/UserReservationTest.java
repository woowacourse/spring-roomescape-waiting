package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(FixedClockConfig.class)
@Sql(scripts = "/testReservationData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserReservationTest {

    @Test
    @DisplayName("이름으로 내 예약 목록을 조회한다.")
    void getMyReservations() {
        RestAssured.given().log().all()
                .queryParam("name", "user_a")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("user_a"));
    }

    @Test
    @DisplayName("존재하지 않는 이름으로 조회하면 빈 목록을 반환한다.")
    void getMyReservationsWithUnknownName() {
        RestAssured.given().log().all()
                .queryParam("name", "unknown")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("미래 예약을 취소한다.")
    void cancelFutureReservation() {
        RestAssured.given().log().all()
                .queryParam("name", "user_b")
                .when().delete("/reservations/2")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("지난 예약 취소 시 400을 반환한다.")
    void cancelPastReservation() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("존재하지 않는 예약 취소 시 404를 반환한다.")
    void cancelNonExistentReservation() {
        RestAssured.given().log().all()
                .queryParam("name", "녀녕")
                .when().delete("/reservations/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @DisplayName("예약의 날짜와 시간을 변경한다.")
    void updateReservation() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-07-01");
        params.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "user_b")
                .body(params)
                .when().patch("/reservations/2")
                .then().log().all()
                .statusCode(200)
                .body("date", is("2026-07-01"))
                .body("time.id", is(1));
    }

    @Test
    @DisplayName("지난 시간으로 변경 시 400을 반환한다.")
    void updateReservationToPastTime() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-04-01");
        params.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/2")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("이미 차있는 시간으로 변경 시 409를 반환한다.")
    void updateReservationToDuplicateSlot() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "user_b")
                .body(params)
                .when().patch("/reservations/2")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("존재하지 않는 예약 변경 시 404를 반환한다.")
    void updateNonExistentReservation() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-07-01");
        params.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "녀녕")
                .body(params)
                .when().patch("/reservations/999")
                .then().log().all()
                .statusCode(404);
    }


    @Test
    @DisplayName("예약과 예약 대기가 같이 조회되는지 확인한다.")
    void getMyReservationsAndWaitingTest() {
        RestAssured.given().log().all()
                .queryParam("name", "user_b")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].status", is("예약대기"))
                .body("[0].rank", is(2))
                .body("[1].status", is("예약"));
    }
}
