package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.TestFixture.MEMBER1;
import static roomescape.TestFixture.MEMBER1_LOGIN_REQUEST;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.RESERVATION_TIME_11AM;
import static roomescape.TestFixture.THEME1;
import static roomescape.TestFixture.TOMORROW;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.BaseControllerTest;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;

class ReservationTimeControllerTest extends BaseControllerTest {

    @DisplayName("모든 시간을 조회한다.")
    @Test
    void findAllReservationTime() {
        // given
        timeRepository.save(RESERVATION_TIME_10AM);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getMember1WithToken())
                .when().get("/times")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("count", is(1));
    }

    @DisplayName("예약 가능한 시간을 조회한다.")
    @Test
    void findAvailableReservationTime() {
        // given
        Theme theme = themeRepository.save(THEME1);
        ReservationTime time1 = timeRepository.save(RESERVATION_TIME_10AM);
        ReservationTime time2 = timeRepository.save(RESERVATION_TIME_11AM);

        Member member = memberRepository.save(MEMBER1);
        String accessToken = TestFixture.getTokenAfterLogin(MEMBER1_LOGIN_REQUEST);

        // 1번 시간에 대해서만 예약을 추가한다. 1번 시간의 booked는 true가 되고, 2번 시간의 booked는 false가 된다.
        reservationRepository.save(new Reservation(member, TOMORROW, time1, theme, Status.CONFIRMED));

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .param("themeId", theme.getId())
                .param("date", TOMORROW.toString())
                .when().get("/times/filter")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("responses[0].booked", is(true))
                .body("responses[1].booked", is(false));
    }
}
