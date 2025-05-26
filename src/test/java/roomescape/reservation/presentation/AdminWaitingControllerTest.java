package roomescape.reservation.presentation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DatabaseCleaner;
import roomescape.member.presentation.fixture.MemberFixture;
import roomescape.reservation.presentation.fixture.ReservationFixture;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AdminWaitingControllerTest {

    private final DatabaseCleaner databaseCleaner;
    private final ReservationFixture reservationFixture = new ReservationFixture();
    private final MemberFixture memberFixture = new MemberFixture();

    @LocalServerPort
    int port;

    @Autowired
    AdminWaitingControllerTest(final DatabaseCleaner databaseCleaner) {
        this.databaseCleaner = databaseCleaner;
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clear();
        databaseCleaner.setUserInfo();
    }

    @Test
    @DisplayName("어드민 예약 대기 관리 목록 조회 테스트")
    void getWaitingsTest() {
        // given
        final Map<String, String> adminCookies = memberFixture.loginAdmin();
        final Map<String, String> userCookies = memberFixture.loginUser();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), adminCookies);

        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                adminCookies
        );

        reservationFixture.createReservation(LocalDate.of(2025, 8, 5), 1L, 1L, adminCookies);
        reservationFixture.createWaiting(LocalDate.of(2025, 8, 5), 1L, 1L, userCookies);

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(adminCookies)
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("대기 승인 테스트")
    void acceptWaitingTest() {
        // given
        final Map<String, String> adminCookies = memberFixture.loginAdmin();
        final Map<String, String> userCookies = memberFixture.loginUser();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), adminCookies);

        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                adminCookies
        );

        reservationFixture.createReservation(LocalDate.of(2025, 8, 5), 1L, 1L, adminCookies);
        reservationFixture.createWaiting(LocalDate.of(2025, 8, 5), 1L, 1L, userCookies);

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(adminCookies)
                .when().delete("/admin/reservations/waiting/accept/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("대기 거절 테스트")
    void rejectWaitingTest() {
        // given
        final Map<String, String> adminCookies = memberFixture.loginAdmin();
        final Map<String, String> userCookies = memberFixture.loginUser();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), adminCookies);

        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                adminCookies
        );

        reservationFixture.createReservation(LocalDate.of(2025, 8, 5), 1L, 1L, adminCookies);
        reservationFixture.createWaiting(LocalDate.of(2025, 8, 5), 1L, 1L, userCookies);

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(adminCookies)
                .when().delete("/admin/reservations/waiting/reject/1")
                .then().log().all()
                .statusCode(204);
    }
}
