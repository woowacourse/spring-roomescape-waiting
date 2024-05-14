package roomescape.reservation.integration;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import roomescape.auth.domain.Token;
import roomescape.auth.provider.CookieProvider;
import roomescape.model.IntegrationTest;
import roomescape.reservation.dto.ReservationRequest;

class ReservationIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("정상적인 요청에 대하여 예약을 정상적으로 등록, 조회, 삭제한다.")
    void adminReservationPageWork() {
        Token token = tokenProvider.getAccessToken(1);
        ResponseCookie cookie = CookieProvider.setCookieFrom(token);

        ReservationRequest reservationRequest = new ReservationRequest(TODAY.plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie.toString())
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));

        RestAssured.given().log().all()
                .when().delete("/reservations/3")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
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
}
