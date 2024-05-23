package roomescape.domain.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
                .body("size()", is(5));
    }

    @DisplayName("필터링된 예약 목록을 불러올 수 있다.")
    @Test
    void should_response_filtering_reservation_list_when_request_reservations() {
        RestAssured.given().log().all()
                .queryParam("themeId", 4)
                .queryParam("memberId", 2)
                .queryParam("dateFrom", "2024-05-10")
                .queryParam("dateTo", "2024-05-11")
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

    @DisplayName("예약 가능 시각 목록을 불러올 수 있다. (200 OK)")
    @Test
    void should_response_bookable_time() {
        RestAssured.given().log().all()
                .when().get("/bookable-times?date=2024-05-10&themeId=4")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }

    @DisplayName("존재하는 리소스에 대한 삭제 요청시, 204 no content를 응답한다.")
    @Test
    void should_remove_reservation_when_delete_request_reservations_id() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 리소스에 대한 삭제 요청시, 404 Not Found를 응답한다.")
    @Test
    void should_response_bad_request_when_nonExist_id() {
        RestAssured.given().log().all()
                .when().delete("/reservations/6")
                .then().log().all()
                .statusCode(404);
    }
}
