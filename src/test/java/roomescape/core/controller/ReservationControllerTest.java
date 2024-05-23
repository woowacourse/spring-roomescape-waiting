package roomescape.core.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.core.dto.reservation.MemberReservationRequest;
import roomescape.core.dto.reservationtime.ReservationTimeRequest;
import roomescape.core.dto.waiting.MemberWaitingRequest;
import roomescape.utils.AccessTokenGenerator;
import roomescape.utils.AdminGenerator;
import roomescape.utils.DatabaseCleaner;
import roomescape.utils.ReservationRequestGenerator;
import roomescape.utils.ReservationTimeRequestGenerator;
import roomescape.utils.TestFixture;
import roomescape.utils.ThemeRequestGenerator;

@AcceptanceTest
class ReservationControllerTest {
    private static final String TOMORROW = TestFixture.getTomorrowDate();
    private static final String DAY_AFTER_TOMORROW = TestFixture.getDayAfterTomorrowDate();

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private AdminGenerator adminGenerator;

    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        adminGenerator.generate();
        accessToken = AccessTokenGenerator.generate();

        ThemeRequestGenerator.generateWithName("테마 1");
        ReservationTimeRequestGenerator.generateOneMinuteAfter();
        ReservationRequestGenerator.generateWithTimeAndTheme(1L, 1L);
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.executeTruncate();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "abc"})
    @DisplayName("예약 생성 시, date의 형식이 올바르지 않으면 예외가 발생한다.")
    void validateReservationWithDateFormat(final String date) {
        MemberReservationRequest request = new MemberReservationRequest(date, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, date가 이미 지난 날짜면 예외가 발생한다.")
    void validateReservationWithPastDate() {
        MemberReservationRequest request = new MemberReservationRequest("2020-10-10", 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, date는 오늘이고 time은 이미 지난 시간이면 예외가 발생한다.")
    void validateReservationWithTodayPastTime() {
        ReservationTimeRequest timeRequest = new ReservationTimeRequest(
                LocalTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm")));

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        MemberReservationRequest memberReservationRequest = new MemberReservationRequest(
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                4L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(memberReservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }


    @Test
    @DisplayName("예약 생성 시, timeId가 null이면 예외가 발생한다.")
    void validateReservationWithNullTimeId() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, null, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, timeId 값으로 찾을 수 있는 시간이 없으면 예외가 발생한다.")
    void validateReservationWithTimeIdNotFound() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 0L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, 해당 날짜와 시간에 예약 내역이 있으면 예외가 발생한다.")
    void validateReservationWithDuplicatedDateAndTime() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, themeId가 null이면 예외가 발생한다.")
    void validateReservationWithNullThemeId() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 1L, null);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, themeId 값으로 찾을 수 있는 테마가 없으면 예외가 발생한다.")
    void validateReservationWithThemeIdNotFound() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 1L, 0L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("모든 예약 내역을 조회한다.")
    void findAllReservations() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void validateReservationDelete() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("조건에 따라 예약을 조회한다.")
    void findReservationsByCondition() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        MemberReservationRequest request2 = new MemberReservationRequest(DAY_AFTER_TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request2)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .queryParams(
                        "memberId", 1L,
                        "themeId", 1L,
                        "dateFrom", TOMORROW,
                        "dateTo", DAY_AFTER_TOMORROW
                )
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("토큰이 유효하지 않을 경우 예외가 발생한다.")
    void validateToken() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", "invalid-token")
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("현재 로그인된 회원의 예약 대기를 포함한 예약 목록을 조회한다.")
    void findLoginMemberReservation() {
        MemberWaitingRequest waitingRequest = new MemberWaitingRequest(TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }
}
