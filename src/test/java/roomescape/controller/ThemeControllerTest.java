package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.TestFixture.MEMBER1;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.THEME1;
import static roomescape.TestFixture.THEME2;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.BaseControllerTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;

class ThemeControllerTest extends BaseControllerTest {

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void findAllThemes() {
        // given
        themeRepository.save(THEME1);

        RestAssured.given().log().all()
                .header("cookie", getMember1WithToken())
                .when().get("/themes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("count", is(1));
    }

    @DisplayName("지난 일주일간 가장 많이 예약된 테마를 조회한다.")
    @Test
    void findMostReservedThemes() {
        // given
        Member member = memberRepository.save(MEMBER1);
        ReservationTime time = timeRepository.save(RESERVATION_TIME_10AM);
        Theme theme1 = themeRepository.save(THEME1);
        Theme theme2 = themeRepository.save(THEME2);

        // 테마 1번은 오늘 날짜이므로, 조회되지 않아야 한다.
        reservationRepository.save(new Reservation(member, LocalDate.now(), time, theme1, Status.CONFIRMED));
        reservationRepository.save(
                new Reservation(member, LocalDate.now().minusDays(1), time, theme2, Status.CONFIRMED));

        RestAssured.given().log().all()
                .when().get("/themes/ranking")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("count", is(1));
    }
}
