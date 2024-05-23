package roomescape.domain.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.ControllerTest;
import roomescape.domain.reservation.dto.ReservationAddRequest;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;

class ReservationControllerTest extends ControllerTest {

    @DisplayName("예약 목록을 불러올 수 있다.")
    @Test
    void should_response_reservation_list_when_request_reservations() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(20));
    }

    @DisplayName("특정 멤버의 예약 및 예약대기 목록을 불러올 수 있다.")
    @Test
    void should_response_reservation_list_specific_member_when_request_reservations() {
        String cookie = getMemberCookie();

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().get("/reservations/all")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(6));
    }

    @DisplayName("필터링된 예약 목록을 불러올 수 있다.")
    @Test
    void should_response_filtering_reservation_list_when_request_reservations() {
        RestAssured.given().log().all()
                .queryParam("themeId", 4)
                .queryParam("memberId", 2)
                .queryParam("dateFrom", "2025-05-10")
                .queryParam("dateTo", "2025-05-12")
                .when().get("/reservations/search")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("멤버의 예약을 추가를 성공할 시, 201 ok를 응답한다,")
    @Test
    void should_add_reservation_when_post_request_member_reservations() {
        String cookie = getMemberCookie();

        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(
                LocalDate.MAX, 1L, 1L, null);
        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .contentType(ContentType.JSON)
                .body(reservationAddRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("status", is("RESERVATION"));
    }

    @DisplayName("멤버의 예약대기를 추가를 성공할 시, 201 ok를 응답한다,")
    @Test
    void should_add_reservation_wait_when_post_request_member_reservations() {
        String cookie = getMemberCookie();

        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(
                LocalDate.of(2025, 5, 13), 3L, 2L, null);
        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .contentType(ContentType.JSON)
                .body(reservationAddRequest)
                .when().post("/reservations/wait")
                .then().log().all()
                .statusCode(201)
                .body("status", is("RESERVATION_WAIT"));
    }

    @DisplayName("존재하는 예약대기에 대한 삭제 요청시, 204 no content를 응답한다.")
    @Test
    void should_remove_reservation_wait_when_delete_request_reservations_id() {
        RestAssured.given().log().all()
                .when().delete("/reservations/wait/9")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("존재하지 않는 예약대기에 대한 삭제 요청시, 404 Not Found를 응답한다.")
    @Test
    void should_response_not_found_when_nonExist_id() {
        RestAssured.given().log().all()
                .when().delete("/reservations/wait/21")
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @DisplayName("예약 삭제 요청 시, 500 Internal Server Error를 응답한다.")
    @Test
    void should_response_internal_server_error_when_delete_reservation() {
        RestAssured.given().log().all()
                .when().delete("/reservations/wait/1")
                .then().log().all()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
