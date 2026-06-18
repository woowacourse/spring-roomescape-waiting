package roomescape.domain.reservationslot;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.TestFixture;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationSlotIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestFixture testFixture;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        testFixture.clear();
    }

    @Test
    @DisplayName("예약 슬롯 조회를 end-to-end로 확인한다.")
    void getReservationSlots() {
        Theme theme = testFixture.saveTheme("공포");
        ReservationDate date = testFixture.saveDate("2026-06-01");
        ReservationTime firstTime = testFixture.saveTime("10:00");
        ReservationTime secondTime = testFixture.saveTime("11:00");
        ReservationSlot reservationSlot = testFixture.saveSlot(date, firstTime, theme);
        testFixture.saveReservation("보예", reservationSlot, ReservationStatus.CONFIRMED);

        given().log().all()
            .contentType(ContentType.JSON)
            .param("themeId", theme.getId())
            .param("dateId", date.getId())
            .when().get("/reservation-slots")
            .then().log().all()
            .statusCode(200)
            .body("[0].timeId", is(firstTime.getId().intValue()))
            .body("[0].startAt", is("10:00"))
            .body("[0].waitingNumber", is(1))
            .body("[1].timeId", is(secondTime.getId().intValue()))
            .body("[1].startAt", is("11:00"))
            .body("[1].waitingNumber", is(0));
    }

    @Test
    @DisplayName("예약 슬롯만 있고 실제 예약이 없으면 예약 인원은 0명으로 조회된다.")
    void getReservationSlotsWhenReservationSlotHasNoReservation() {
        Theme theme = testFixture.saveTheme("공포");
        ReservationDate date = testFixture.saveDate("2026-06-01");
        ReservationTime time = testFixture.saveTime("10:00");
        testFixture.saveSlot(date, time, theme);

        given().log().all()
            .contentType(ContentType.JSON)
            .param("themeId", theme.getId())
            .param("dateId", date.getId())
            .when().get("/reservation-slots")
            .then().log().all()
            .statusCode(200)
            .body("[0].timeId", is(time.getId().intValue()))
            .body("[0].startAt", is("10:00"))
            .body("[0].waitingNumber", is(0));
    }

    @Test
    @DisplayName("예약 슬롯 조회 시 themeId 파라미터가 누락되었을 경우 400 에러가 발생한다.")
    void getReservationSlotsWithoutThemeId() {
        given().log().all()
            .contentType(ContentType.JSON)
            .param("dateId", 1L)
            .when().get("/reservation-slots")
            .then().log().all()
            .statusCode(400)
            .body("code", is("REQUIRED_PARAMETER_MISSING"))
            .body("message", is("필수 요청 파라미터가 누락되었습니다."));
    }

    @Test
    @DisplayName("예약 슬롯 조회 시 존재하지 않는 테마일 경우 404 에러가 발생한다.")
    void getReservationSlotsWhenThemeNotFound() {
        ReservationDate date = testFixture.saveDate("2026-06-01");

        given().log().all()
            .contentType(ContentType.JSON)
            .param("themeId", 999L)
            .param("dateId", date.getId())
            .when().get("/reservation-slots")
            .then().log().all()
            .statusCode(404)
            .body("code", is("THEME_NOT_EXIST"))
            .body("message", is("존재하지 않는 테마 입니다."));
    }
}
