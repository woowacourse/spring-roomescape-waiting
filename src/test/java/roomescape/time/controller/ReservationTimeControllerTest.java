package roomescape.time.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.AcceptanceTest;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.slot.fixture.SlotApiFixture.createSlot;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;
import static roomescape.time.fixture.ReservationTimeApiFixture.createReservationTime;
import static roomescape.time.fixture.ReservationTimeApiFixture.updateTimeStatus;

class ReservationTimeControllerTest extends AcceptanceTest {

    private static String startAt1 = "10:00:00";
    private static String startAt2 = "11:00:00";
    private static String themeName = "테마1";

    @Test
    @DisplayName("사용자는 특정 날짜와 테마의 예약 가능한 시간을 조회한다.")
    void readAvailableTimes() {
        Integer dateId = createReservationDate(managerToken, LocalDate.now().plusDays(1).toString());
        Integer timeId = createReservationTime(managerToken, startAt1);
        updateTimeStatus(managerToken, timeId, true);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);

        RestAssured.given().log().all()
                .queryParam("dateId", dateId)
                .queryParam("themeId", themeId)
                .when().get("/member/slots/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].timeId", is(timeId))
                .body("[0].slotId", is(slotId))
                .body("[0].startAt", is(startAt1))
                .body("[0].isActive", is(true));
    }

    @Test
    @DisplayName("예약 시간이 없으면 빈 목록을 반환한다.")
    void readAvailableTimesEmpty() {
        Integer dateId = createReservationDate(managerToken, LocalDate.now().plusDays(1).toString());
        Integer themeId = createTheme(managerToken, themeName);

        RestAssured.given().log().all()
                .queryParam("dateId", dateId)
                .queryParam("themeId", themeId)
                .when().get("/member/slots/times")
                .then().log().all()
                .statusCode(200);
    }

}
