package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.date.exception.ReservationDateErrorInformation.INACTIVE_DATE_NOT_ALLOWED;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.date.fixture.ReservationDateApiFixture.updateDateStatus;
import static roomescape.reservation.exception.ReservaitonErrorInformation.*;
import static roomescape.reservation.fixture.ReservationApiFixture.cancelReservationWithToken;
import static roomescape.reservation.fixture.ReservationApiFixture.createReservationWithToken;
import static roomescape.theme.exception.ThemeErrorInformation.INACTIVE_THEME_NOT_ALLOWED;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;
import static roomescape.theme.fixture.ThemeApiFixture.updateThemeStatus;
import static roomescape.time.exception.ReservationTimeErrorInformation.INACTIVE_TIME_NOT_ALLOWED;
import static roomescape.time.fixture.ReservationTimeApiFixture.createReservationTime;
import static roomescape.time.fixture.ReservationTimeApiFixture.updateTimeStatus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.AcceptanceTest;

class ReservationControllerTest extends AcceptanceTest {

    private final String reservationName = "브라운";

    private final String date = LocalDate.of(2099, 1, 1).toString();
    private final String startAt = "10:00";
    private final String otherStartAt = "11:00";

    private final String themeName = "테마1";

    @Test
    @DisplayName("사용자는 예약을 생성한다.")
    void create_reservation() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        createReservationWithToken(memberToken, dateId, timeId, themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .when().get("/member/my-reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("사용자는 자신의 이름으로 예약 목록을 조회한다.")
    void get_my_reservations() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer themeId = createTheme(managerToken, themeName);

        Integer timeId = createReservationTime(managerToken, startAt);
        Integer otherTimeId = createReservationTime(managerToken, otherStartAt);

        createReservationWithToken(memberToken, dateId, timeId, themeId);
        createReservationWithToken(anotherToken, dateId, otherTimeId, themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .when().get("/member/my-reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, anotherToken)
                .when().get("/member/my-reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("예약이 없는 이름으로 조회하면 빈 목록을 반환한다.")
    void get_my_reservations_empty() {
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .when().get("/member/my-reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("dateId가 없으면 예약 생성에 실패한다.")
    void create_reservation_without_date_id() {
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", null);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/reservations")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", is("요청 값 검증에 실패했습니다."));
    }

    @Test
    @DisplayName("timeId가 없으면 예약 생성에 실패한다.")
    void create_reservation_without_time_id() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer themeId = createTheme(managerToken, themeName);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        params.put("timeId", null);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/reservations")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", is("요청 값 검증에 실패했습니다."));
    }

    @Test
    @DisplayName("themeId가 없으면 예약 생성에 실패한다.")
    void create_reservation_without_theme_id() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        params.put("timeId", timeId);
        params.put("themeId", null);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/reservations")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", is("요청 값 검증에 실패했습니다."));
    }

    @Test
    @DisplayName("예약된 날짜/시간/테마를 중복 예약하면 예외가 발생한다.")
    void reserved_duplicated() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);

        createReservationWithToken(memberToken, dateId, timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/reservations")
                .then().log().all()
                .statusCode(RESERVATION_ALREADY_BOOKED.getHttpStatus().value())
                .body("message", is(RESERVATION_ALREADY_BOOKED.getMessage()));
    }

    @Test
    @DisplayName("취소된 예약을 동일한 사람이 새롭게 예약할 수 있다.")
    void reserved_when_canceled_same_name() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);

        Integer reservationId = createReservationWithToken(memberToken, dateId, timeId, themeId);
        cancelReservationWithToken(memberToken, reservationId);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/reservations")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("취소된 예약을 동일한 사람이 새롭게 예약할 수 있다.")
    void reserved_when_canceled_another_name() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);

        Integer reservationId = createReservationWithToken(memberToken, dateId, timeId, themeId);
        cancelReservationWithToken(memberToken, reservationId);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, anotherToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/reservations")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("사용자는 자신의 예약을 취소한다.")
    void cancel() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);

        Integer reservationId = createReservationWithToken(memberToken, dateId, timeId, themeId);

        Map<String, String> params = new HashMap<>();
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/member/reservations/" + reservationId + "/cancel")
                .then().log().all()
                .statusCode(200)
                .body("status", is("CANCELED"));
    }

    @Test
    @DisplayName("본인의 예약이 아닌데 취소하면 예외가 발생한다.")
    void cancel_not_owner() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);

        Integer reservationId = createReservationWithToken(memberToken, dateId, timeId, themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, anotherToken)
                .contentType(ContentType.JSON)
                .when().patch("/member/reservations/" + reservationId + "/cancel")
                .then().log().all()
                .statusCode(RESERVATION_NOT_OWNER.getHttpStatus().value())
                .body("message", is(RESERVATION_NOT_OWNER.getMessage()));
    }

    @Test
    @DisplayName("이미 취소된 예약을 취소하면 예외가 발생한다.")
    void cancel_already_canceled() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);

        Integer reservationId = createReservationWithToken(memberToken, dateId, timeId, themeId);
        cancelReservationWithToken(memberToken, reservationId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .when().patch("/member/reservations/" + reservationId + "/cancel")
                .then().log().all()
                .statusCode(RESERVATION_ALREADY_CANCELED.getHttpStatus().value())
                .body("message", is(RESERVATION_ALREADY_CANCELED.getMessage()));
    }

    @Test
    @DisplayName("이미 지난 예약을 취소하면 예외가 발생한다.")
    @Sql(
            scripts = {"classpath:truncate.sql", "classpath:test-member.sql", "classpath:past-reservation.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    void cancel_not_past() {
        Long sqlSavedId = 1L;

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .when().patch("/member/reservations/" + sqlSavedId + "/cancel")
                .then().log().all()
                .statusCode(RESERVATION_ALREADY_PAST.getHttpStatus().value())
                .body("message", is(RESERVATION_ALREADY_PAST.getMessage()));
    }

    @Test
    @DisplayName("예약 가능한 날짜로 변경할 수 있다.")
    void changeSchedule() {
        String futureDate = LocalDate.now().plusDays(1).toString();
        String futureTime = LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString();
        Integer dateId = createReservationDate(managerToken, date);
        Integer changedDateId = createReservationDate(managerToken, futureDate);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer changedTimeId = createReservationTime(managerToken, futureTime);
        Integer themeId = createTheme(managerToken, themeName);
        Integer reservationId = createReservationWithToken(memberToken, dateId, timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", changedDateId);
        params.put("timeId", changedTimeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/member/reservations/" + reservationId + "/schedule?name=" + reservationName)
                .then().log().all()
                .statusCode(200)
                .body("date", is(futureDate))
                .body("time", is(futureTime));
    }

    @Test
    @DisplayName("본인의 예약이 아닌데 변경을 시도하면 예외가 발생한다.")
    void changeSchedule_not_owner() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer changedDateId = createReservationDate(managerToken, LocalDate.now().plusDays(1).toString());
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer changedTimeId = createReservationTime(managerToken, LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());
        Integer themeId = createTheme(managerToken, themeName);
        Integer reservationId = createReservationWithToken(memberToken, dateId, timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", changedDateId);
        params.put("timeId", changedTimeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, anotherToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/member/reservations/" + reservationId + "/schedule")
                .then().log().all()
                .statusCode(RESERVATION_NOT_OWNER.getHttpStatus().value())
                .body("message", is(RESERVATION_NOT_OWNER.getMessage()));
    }

    @Test
    @DisplayName("이미 취소된 예약을 변경하면 예외가 발생한다.")
    void changeSchedule_already_canceled() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer changedDateId = createReservationDate(managerToken, LocalDate.now().plusDays(1).toString());
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer changedTimeId = createReservationTime(managerToken, LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());
        Integer themeId = createTheme(managerToken, themeName);
        Integer reservationId = createReservationWithToken(memberToken, dateId, timeId, themeId);
        cancelReservationWithToken(memberToken, reservationId);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", changedDateId);
        params.put("timeId", changedTimeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/member/reservations/" + reservationId + "/schedule")
                .then().log().all()
                .statusCode(RESERVATION_ALREADY_CANCELED.getHttpStatus().value())
                .body("message", is(RESERVATION_ALREADY_CANCELED.getMessage()));
    }

    @Test
    @DisplayName("이미 지난 예약을 변경하면 예외가 발생한다.")
    @Sql(
            scripts = {"classpath:truncate.sql", "classpath:test-member.sql", "classpath:past-reservation.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    void changeSchedule_past() {
        Integer changedDateId = createReservationDate(managerToken, LocalDate.now().plusDays(1).toString());
        Integer changedTimeId = createReservationTime(managerToken, LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());

        Long sqlSavedId = 1L;

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", changedDateId);
        params.put("timeId", changedTimeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/member/reservations/" + sqlSavedId + "/schedule")
                .then().log().all()
                .statusCode(RESERVATION_ALREADY_PAST.getHttpStatus().value())
                .body("message", is(RESERVATION_ALREADY_PAST.getMessage()));
    }

    @Test
    @DisplayName("지난 날짜/시간으로 예약을 변경하면 예외가 발생한다.")
    @Sql(
            scripts = {"classpath:truncate.sql", "classpath:test-member.sql", "classpath:past-reservation-date.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    void changeSchedule_new_datetime_is_past() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer pastDateId = 1;
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer changedTimeId = createReservationTime(managerToken, LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());
        Integer themeId = createTheme(managerToken, themeName);
        Integer reservationId = createReservationWithToken(memberToken, dateId, timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", pastDateId);
        params.put("timeId", changedTimeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/member/reservations/" + reservationId + "/schedule")
                .then().log().all()
                .statusCode(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getHttpStatus().value())
                .body("message", is(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getMessage()));
    }

    @Test
    @DisplayName("비활성화된 날짜로 예약을 생성하면 예외가 발생한다.")
    void create_reservation_with_inactive_date() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        updateDateStatus(managerToken, dateId, false);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/reservations")
                .then().log().all()
                .statusCode(INACTIVE_DATE_NOT_ALLOWED.getHttpStatus().value())
                .body("message", is(INACTIVE_DATE_NOT_ALLOWED.getMessage()));
    }

    @Test
    @DisplayName("비활성화된 시간으로 예약을 생성하면 예외가 발생한다.")
    void create_reservation_with_inactive_time() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        updateTimeStatus(managerToken, timeId, false);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/reservations")
                .then().log().all()
                .statusCode(INACTIVE_TIME_NOT_ALLOWED.getHttpStatus().value())
                .body("message", is(INACTIVE_TIME_NOT_ALLOWED.getMessage()));
    }

    @Test
    @DisplayName("비활성화된 테마로 예약을 생성하면 예외가 발생한다.")
    void create_reservation_with_inactive_theme() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        updateThemeStatus(managerToken, themeId, false);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/reservations")
                .then().log().all()
                .statusCode(INACTIVE_THEME_NOT_ALLOWED.getHttpStatus().value())
                .body("message", is(INACTIVE_THEME_NOT_ALLOWED.getMessage()));
    }

}
