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
import roomescape.domain.reservation.Reservation;
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
    @DisplayName("관리자의 예약 전체 조회를 end-to-end로 확인한다.")
    void getAllReservation() {
        saveThemeDateTimeAndReservation("보예");

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(200)
            .body("[0].date", is("2026-06-01"))
            .body("[0].time.startAt", is("10:00"))
            .body("[0].theme.name", is("공포"));
    }

    @Test
    @DisplayName("관리자가 토큰을 누락했을 경우 401 예외가 발생한다.")
    void getAllReservationWithoutToken() {
        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    @DisplayName("관리자의 예약 취소를 end-to-end로 확인한다.")
    void deleteReservation() {
        Long reservationId = saveThemeDateTimeAndReservation("보예");

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().delete("/admin/reservations/{id}", reservationId)
            .then().log().all()
            .statusCode(204);

        assertThat(testFixture.findReservationStatus(reservationId)).isEqualTo(ReservationStatus.CANCELED);
    }

    private Long saveThemeDateTimeAndReservation(String name) {
        Reservation reservation = testFixture.saveReservation(name, "2026-06-01", "10:00", "공포");
        return reservation.getId();
    }
}
