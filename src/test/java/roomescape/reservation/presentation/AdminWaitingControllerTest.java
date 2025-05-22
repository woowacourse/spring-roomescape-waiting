package roomescape.reservation.presentation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.presentation.fixture.MemberFixture;
import roomescape.reservation.presentation.fixture.ReservationFixture;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminWaitingControllerTest {
    private final ReservationFixture reservationFixture = new ReservationFixture();
    private final MemberFixture memberFixture = new MemberFixture();

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
}
