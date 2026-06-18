package roomescape.domain.theme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.support.TestFixture;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ThemeIntegrationTest {

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
    @DisplayName("전체 테마 조회를 end-to-end로 확인한다.")
    void getAllTheme() {
        testFixture.saveTheme("공포");
        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/themes")
            .then().log().all()
            .statusCode(200)
            .body("[0].name", is("공포"))
            .body("[0].content", is("무서운 테마"))
            .body("[0].url", is("theme-url"));
    }

    @Test
    @DisplayName("인기 테마 조회는 예약 슬롯 id와 예약 id가 달라도 실제 예약 슬롯 기준으로 집계한다.")
    void getThemeRankByReservationSlotId() {
        LocalDate reservationDate = LocalDate.now().minusDays(1);
        Theme theme = testFixture.saveTheme("공포");
        Theme dummyTheme = testFixture.saveTheme("보예", "보예 테마", "boye-url");
        ReservationDate date = testFixture.saveDate(reservationDate.toString());
        ReservationTime time = testFixture.saveTime("10:00");
        testFixture.saveSlot(date, time, dummyTheme);
        ReservationSlot targetSlot = testFixture.saveSlot(date, time, theme);
        testFixture.saveReservation("보예", targetSlot, ReservationStatus.CONFIRMED);

        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/themes/rank")
            .then().log().all()
            .statusCode(200)
            .body("[0].themeName", is("공포"));
    }
}
