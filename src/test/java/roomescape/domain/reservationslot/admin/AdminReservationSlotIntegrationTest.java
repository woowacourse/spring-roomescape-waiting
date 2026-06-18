package roomescape.domain.reservationslot.admin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.support.TestFixture;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AdminReservationSlotIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestFixture testFixture;

    @Value("${token}")
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        testFixture.clear();
    }

    @Test
    @DisplayName("관리자의 대기 목록 조회를 end-to-end로 확인한다.")
    void getWaitingReservations() {
        var theme = testFixture.saveTheme("공포");
        var date = testFixture.saveDate("2026-06-01");
        var time = testFixture.saveTime("10:00");
        var slot = testFixture.saveSlot(date, time, theme);
        testFixture.saveReservation("보예", slot, ReservationStatus.CONFIRMED);
        testFixture.saveReservation("수민", slot, ReservationStatus.WAITING);

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/waitings")
            .then().log().all()
            .statusCode(200)
            .body("[0].theme.name", is("공포"))
            .body("[0].userName", is("수민"))
            .body("[0].waitingNumber", is(1))
            .body("[0].reservationStatus", is("WAITING"));
    }

    @Test
    @DisplayName("관리자의 대기 예약 취소를 end-to-end로 확인한다.")
    void deleteWaitingReservation() {
        var theme = testFixture.saveTheme("공포");
        var date = testFixture.saveDate("2026-06-01");
        var time = testFixture.saveTime("10:00");
        var slot = testFixture.saveSlot(date, time, theme);
        Long reservationId = testFixture.saveReservation("보예", slot, ReservationStatus.WAITING).getId();

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().delete("/admin/waitings/{id}", reservationId)
            .then().log().all()
            .statusCode(204);

        assertThat(testFixture.findReservationStatus(reservationId)).isEqualTo(ReservationStatus.CANCELED);
    }
}
