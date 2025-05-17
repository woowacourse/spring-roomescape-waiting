package roomescape.reservation.presentation;

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
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.fixture.ReservationFixture;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationControllerTest {
    private final ReservationFixture reservationFixture = new ReservationFixture();
    private final MemberFixture memberFixture = new MemberFixture();

    @Test
    @DisplayName("어드민 예약 추가 테스트")
    void createReservationTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), cookies);

        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                cookies
        );

        final AdminReservationRequest reservation = reservationFixture.createAdminReservationRequest(
                LocalDate.of(2025, 8, 5), 1L,
                1L, 1L);

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("과거 날짜로 예약 추가 시 실패 테스트")
    void createReservationWithPastDateTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), cookies);

        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                cookies
        );

        final AdminReservationRequest reservation = reservationFixture.createAdminReservationRequest(
                LocalDate.of(2020, 1, 1), 1L, 1L, 1L);

        // when - then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservation)
                .when().post("/admin/reservations")
                .then()
                .statusCode(400);
    }
}
