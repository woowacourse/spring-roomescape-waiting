package roomescape.time.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;
import static roomescape.time.fixture.ReservationTimeApiFixture.createReservationTime;
import static roomescape.time.fixture.ReservationTimeApiFixture.updateTimeStatus;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.common.AcceptanceTest;

class ReservationTimeControllerTest extends AcceptanceTest {

    private static String startAt1 = "10:00:00";
    private static String startAt2 = "11:00:00";
    private static String themeName = "테마1";


    @Nested
    @DisplayName("readAvailableTimes 메서드는")
    class ReadAvailableTimesTest {


        @Test
        @DisplayName("예약 가능한 시간을 조회한다.")
        void 성공1() {
            Integer dateId = createReservationDate(managerToken,
                LocalDate.now().plusDays(1).toString());
            Integer timeId = createReservationTime(managerToken, startAt1);
            updateTimeStatus(managerToken, timeId, true);
            Integer themeId = createTheme(managerToken, themeName);

            RestAssured.given().log().all()
                .queryParam("dateId", dateId)
                .queryParam("themeId", themeId)
                .when().get("/member/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(timeId))
                .body("[0].startAt", is(startAt1));
        }


        @Test
        @DisplayName("활성화된 예약 시간만 조회한다")
        void 성공2() {
            Integer dateId = createReservationDate(managerToken,
                LocalDate.now().plusDays(1).toString());
            Integer activeTimeId = createReservationTime(managerToken, startAt1);
            Integer inactiveTimeId = createReservationTime(managerToken, startAt2);
            updateTimeStatus(managerToken, activeTimeId, true);
            updateTimeStatus(managerToken, inactiveTimeId, false);
            Integer themeId = createTheme(managerToken, themeName);

            RestAssured.given().log().all()
                .queryParam("dateId", dateId)
                .queryParam("themeId", themeId)
                .when().get("/member/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
        }
    }
}
