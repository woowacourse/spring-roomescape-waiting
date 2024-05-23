package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.Fixture.COOKIE_NAME;
import static roomescape.Fixture.VALID_ADMIN;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;
import static roomescape.Fixture.VALID_WAITING;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminReservationWaitingControllerTest extends ControllerTest {

    @BeforeEach
    void setInitialData() {
        reservationTimeRepository.save(VALID_RESERVATION_TIME);
        themeRepository.save(VALID_THEME);
        memberRepository.save(VALID_MEMBER);
        reservationRepository.save(VALID_RESERVATION);
        reservationWaitingRepository.save(VALID_WAITING);
    }

    @BeforeEach
    void setAdmin() {
        memberRepository.save(VALID_ADMIN);
    }

    @DisplayName("관리자가 전체 예약 대기 정보를 조회한다. -> 200")
    @Test
    void getAllReservationWaiting() {
        RestAssured.given().log().all()
                .cookie(COOKIE_NAME, getAdminToken())
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }
}
