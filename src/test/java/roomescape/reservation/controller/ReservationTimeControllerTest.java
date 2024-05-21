package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.InitialReservationTimeFixture.INITIAL_RESERVATION_TIME_COUNT;
import static roomescape.InitialReservationTimeFixture.NOT_SAVED_TIME;
import static roomescape.InitialReservationTimeFixture.RESERVATION_TIME_1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.dto.ReservationTimeRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("/initial_test_data.sql")
class ReservationTimeControllerTest {

    @Test
    @DisplayName("예약 가능한 시간을 추가한다.")
    void addReservationGetTime() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(NOT_SAVED_TIME.getStartAt());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTimeRequest)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "1500", "15000", "15", "25:00"})
    @DisplayName("예약 가능한 시간이 잘못된 경우 bad request 상태코드를 반환한다.")
    void wrongGetStartAt(String startAt) {
        Map<String, String> invalidReservationTimeRequest = new HashMap<>();
        invalidReservationTimeRequest.put("startAt", startAt);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(invalidReservationTimeRequest)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("중복된 예약 가능한 시간을 추가하는 경우 bad request 상태코드를 반환한다.")
    void wrongGetStartAt() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(RESERVATION_TIME_1.getStartAt());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTimeRequest)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 가능한 시간을 조회한다.")
    void getTimes() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(INITIAL_RESERVATION_TIME_COUNT));
    }

    @Test
    @DisplayName("id와 매칭되는 예약 가능 시간을 삭제한다.")
    void delete() {
        RestAssured.given().log().all()
                .when().delete("/times/11")
                .then().log().all()
                .statusCode(204);
    }
}
