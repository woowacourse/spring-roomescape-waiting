package roomescape.domain.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.ControllerTest;
import roomescape.domain.reservation.dto.ReservationTimeAddRequest;

import java.time.LocalTime;

import static org.hamcrest.Matchers.is;

class AdminReservationTimeControllerTest extends ControllerTest {

    @DisplayName("예약 시간을 추가 성공할 시, 201 created를 응답한다.")
    @Test
    void should_add_reservation_time_to_db() {
        ReservationTimeAddRequest reservationTimeAddRequest = new ReservationTimeAddRequest(LocalTime.of(9, 0));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTimeAddRequest)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("존재하는 예약 시간을 조회시, 200 ok를 응답한다.")
    @Test
    void should_get_reservation_time_list_in_db() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }

    @DisplayName("존재하는 리소스에 대한 삭제 요청시, 204 no content를 응답한다.")
    @Test
    void should_remove_reservation_time_in_db() {
        RestAssured.given().log().all()
                .when().delete("/times/5")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("delete 요청 시 id값이 존재하지 않으면 404 Not found로 응답한다.")
    @Test
    void should_response_bad_request_when_nonExist_id() {
        RestAssured.given().log().all()
                .when().delete("/times/6")
                .then().log().all()
                .statusCode(404);
    }
}
