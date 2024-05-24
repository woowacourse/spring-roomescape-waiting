package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.Fixture.COOKIE_NAME;
import static roomescape.Fixture.VALID_ADMIN;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;
import static roomescape.Fixture.VALID_RESERVATION_DATE;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.web.controller.request.ReservationWaitingWebRequest;

public class AutoWaitingApproveTest extends ControllerTest {

    @BeforeEach
    void setInitialData() {
        reservationTimeRepository.save(VALID_RESERVATION_TIME);
        themeRepository.save(VALID_THEME);
        memberRepository.save(VALID_MEMBER);
        reservationRepository.save(VALID_RESERVATION);

        memberRepository.save(VALID_ADMIN);
    }

    @DisplayName("예약을 삭제시 대기가 있다면, 자동으로 다음 대기자가 승인된다.")
    @Test
    void approveAfterReservationDelete() {

        ReservationWaitingWebRequest request = new ReservationWaitingWebRequest(
                VALID_RESERVATION_DATE.getDate().toString(), 1L, 1L);

        RestAssured.given().log().all()
                .cookie(COOKIE_NAME, getAdminToken())
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all();

        //when
        RestAssured.given().log().all()
                .cookie(COOKIE_NAME, getAdminToken())
                .contentType(ContentType.JSON)
                .when().delete("/admin/reservations/1")
                .then().log().all();

        //then
        RestAssured.given().log().all()
                .cookie(COOKIE_NAME, getAdminToken())
                .contentType(ContentType.JSON)
                .when().get("/reservations")
                .then().log().all()
                .body("[0].name", is(VALID_ADMIN.getName().getName()));
    }
}
