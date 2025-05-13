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
import roomescape.member.presentation.fixture.MemberFixture;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.fixture.ReservationFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationControllerTest {
    private final ReservationFixture reservationFixture = new ReservationFixture();
    private final MemberFixture memberFixture = new MemberFixture();

    @Test
    @DisplayName("예약 추가 테스트")
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

        final ReservationRequest reservation = reservationFixture.createReservationRequest(LocalDate.of(2025, 8, 5), 1L,
                1L);

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("지나간 날짜와 시간에 대한 예약 생성은 불가능하다.")
    void createReservationIsPastDateExceptionTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), cookies);

        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                cookies
        );

        final ReservationRequest reservation = reservationFixture.createReservationRequest(LocalDate.of(2023, 8, 5), 1L,
                1L);

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("중복된 일시의 예약은 불가능하다.")
    void createReservationIsDuplicateDateExceptionTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), cookies);

        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                cookies
        );

        final ReservationRequest reservation = reservationFixture.createReservationRequest(LocalDate.of(2025, 8, 5), 1L,
                1L);

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        // then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간 ID를 이용하여 예약할 수 없다.")
    void createReservationInvalidTimeIdExceptionTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                cookies
        );

        final ReservationRequest reservation = reservationFixture.createReservationRequest(LocalDate.of(2025, 8, 5), 1L,
                1L);

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("존재하지 않는 테마 ID를 이용하여 예약할 수 없다.")
    void createReservationInvalidThemeIdExceptionTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), cookies);

        final ReservationRequest reservation = reservationFixture.createReservationRequest(LocalDate.of(2025, 8, 5), 1L,
                1L);

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 삭제 테스트")
    void deleteReservationTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), cookies);

        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                cookies
        );

        reservationFixture.createReservation(LocalDate.of(2025, 8, 5), 1L, 1L, cookies);

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        // then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("예약 조회 테스트")
    void reservationPageTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();
        reservationFixture.createReservationTime(LocalTime.of(10, 30), cookies);

        reservationFixture.createTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg",
                cookies
        );

        reservationFixture.createReservation(LocalDate.of(2025, 8, 5), 1L, 1L, cookies);

        // when-then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

}
