package roomescape.controller;

import static roomescape.Fixture.COOKIE_NAME;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;
import static roomescape.Fixture.VALID_RESERVATION_DATE;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;
import static roomescape.Fixture.VALID_WAITING;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.web.controller.request.ReservationWaitingWebRequest;

class MemberReservationWaitingControllerTest extends ControllerTest {
    
    @BeforeEach
    void setInitialData() {
        reservationTimeRepository.save(VALID_RESERVATION_TIME);
        themeRepository.save(VALID_THEME);
        memberRepository.save(VALID_MEMBER);
        reservationRepository.save(VALID_RESERVATION);
    }

    @DisplayName("사용자가 예약 대기를 생성한다. -> 201")
    @Test
    void create() {
        ReservationWaitingWebRequest request = new ReservationWaitingWebRequest(
                VALID_RESERVATION_DATE.getDate().toString(), 1L, 1L);

        RestAssured.given().log().all()
                .cookie(COOKIE_NAME, getUserToken())
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("사용자가 예약 대기 중 예외가 발생한다. -> 400")
    @Test
    void create_Fail() {
        reservationWaitingRepository.save(VALID_WAITING);
        ReservationWaitingWebRequest request = new ReservationWaitingWebRequest(
                VALID_RESERVATION_DATE.getDate().toString(), 1L, 1L);

        RestAssured.given().log().all()
                .cookie(COOKIE_NAME, getUserToken())
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예약 대기를 삭제한다. -> 204")
    @Test
    void delete() {
        RestAssured.given().log().all()
                .when().delete("/reservations/waiting/" + 1)
                .then().log().all()
                .statusCode(204);
    }
}
