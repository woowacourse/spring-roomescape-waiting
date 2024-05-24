package roomescape.reservation.integration;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import roomescape.auth.domain.Token;
import roomescape.auth.provider.CookieProvider;
import roomescape.model.IntegrationTest;
import roomescape.reservation.dto.ReservationRequest;

class ReservationIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("예약을 정상적으로 조회한다.")
    void reservationList() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(16));
    }

    @Test
    @DisplayName("예약을 정상적으로 저장한다.")
    void reservationSave() {
        Token token = tokenProvider.getAccessToken(1);
        ResponseCookie cookie = CookieProvider.setCookieFrom(token);

        ReservationRequest reservationRequest = new ReservationRequest(TODAY.plusDays(1), 2L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie.toString())
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("이미 예약한 방탈출을 대기하는 경우 에러가 발생한다.")
    void duplicateReservationWaiting() {
        Token token = tokenProvider.getAccessToken(1);
        ResponseCookie cookie = CookieProvider.setCookieFrom(token);

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2024, 4, 30), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie.toString())
                .body(reservationRequest)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 대기를 정상적으로 저장한다.")
    void reservationWaitingSave() {
        Token token = tokenProvider.getAccessToken(7);
        ResponseCookie cookie = CookieProvider.setCookieFrom(token);

        ReservationRequest reservationRequest = new ReservationRequest(TODAY.plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie.toString())
                .body(reservationRequest)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("예약을 정상적으로 삭제한다.")
    void reservationDelete() {
        RestAssured.given().log().all()
                .when().delete("/reservations/7")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("예약을 요청시 존재하지 않은 예약 시간의 id일 경우 예외가 발생한다.")
    void notExistTime() {
        Token token = tokenProvider.getAccessToken(1);
        ResponseCookie cookie = CookieProvider.setCookieFrom(token);

        ReservationRequest reservationRequest = new ReservationRequest(TODAY, 0L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie.toString())
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("모든 예약 시간 정보를 조회한다.")
    void findReservationTimeList() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/1?date=" + TODAY)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("이미 대기 최대 인원인 경우 예외를 발생한다.")
    void throwException_WhenWaitingCountIsMax() {
        Token token = tokenProvider.getAccessToken(7);
        ResponseCookie cookie = CookieProvider.setCookieFrom(token);

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2025, 5, 20), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie.toString())
                .body(reservationRequest)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(400);
    }
}
