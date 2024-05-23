package roomescape.domain.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.ControllerTest;
import roomescape.domain.reservation.dto.ReservationAddRequest;

import java.time.LocalDate;

class AdminReservationControllerTest extends ControllerTest {

    @DisplayName("어드민의 예약을 추가를 성공할 시, 201 ok를 응답한다,")
    @Test
    void should_add_reservation_when_post_request_admin_reservations() {
        String cookie = getAdminCookie();

        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(
                LocalDate.MAX, 1L, 1L, null);
        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .contentType(ContentType.JSON)
                .body(reservationAddRequest)
                .when().post("admin/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("존재하는 예약에 대한 삭제 요청시, 204 no content를 응답한다.")
    @Test
    void should_remove_reservation_when_delete_request_reservations_id() {
        String cookie = getAdminCookie();

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 예약에 대한 삭제 요청시, 404 Not Found를 응답한다.")
    @Test
    void should_response_bad_request_when_nonExist_id() {
        String cookie = getAdminCookie();

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().delete("/admin/reservations/6")
                .then().log().all()
                .statusCode(404);
    }
}
