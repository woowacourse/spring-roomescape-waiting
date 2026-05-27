package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.date.exception.ReservationDateErrorInformation.INACTIVE_DATE_NOT_ALLOWED;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.date.fixture.ReservationDateApiFixture.updateDateStatus;
import static roomescape.reservation.exception.ReservationErrorInformation.*;
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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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


    @Nested
    @DisplayName("create 메서드는")
    class CreateTest {


        @Test
        @DisplayName("create reservation")
        void 성공() {
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
        @DisplayName("create reservation without date id")
        void 실패1() {
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
        @DisplayName("create reservation without time id")
        void 실패2() {
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
        @DisplayName("create reservation without theme id")
        void 실패3() {
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
        @DisplayName("create reservation with inactive date")
        void 실패4() {
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
        @DisplayName("create reservation with inactive time")
        void 실패5() {
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
        @DisplayName("create reservation with inactive theme")
        void 실패6() {
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

    @Nested
    @DisplayName("get 메서드는")
    class GetTest {


        @Test
        @DisplayName("get my reservations")
        void 성공1() {
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
        @DisplayName("get my reservations empty")
        void 성공2() {
            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .when().get("/member/my-reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
        }
    }

    @Nested
    @DisplayName("reserved 메서드는")
    class ReservedTest {


        @Test
        @DisplayName("reserved duplicated")
        @Disabled
        void 실패() {
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
        @DisplayName("reserved when canceled same name")
        void 성공1() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer themeId = createTheme(managerToken, themeName);

            Integer reservationId = createReservationWithToken(memberToken, dateId, timeId,
                themeId);
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
        @DisplayName("reserved when canceled another name")
        void 성공2() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer themeId = createTheme(managerToken, themeName);

            Integer reservationId = createReservationWithToken(memberToken, dateId, timeId,
                themeId);
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
    }

    @Nested
    @DisplayName("cancel 메서드는")
    class CancelTest {


        @Test
        @DisplayName("cancel")
        void 성공() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer themeId = createTheme(managerToken, themeName);

            Integer reservationId = createReservationWithToken(memberToken, dateId, timeId,
                themeId);

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
        @DisplayName("cancel not owner")
        void 실패1() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer themeId = createTheme(managerToken, themeName);

            Integer reservationId = createReservationWithToken(memberToken, dateId, timeId,
                themeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, anotherToken)
                .contentType(ContentType.JSON)
                .when().patch("/member/reservations/" + reservationId + "/cancel")
                .then().log().all()
                .statusCode(RESERVATION_NOT_OWNER.getHttpStatus().value())
                .body("message", is(RESERVATION_NOT_OWNER.getMessage()));
        }


        @Test
        @DisplayName("cancel already canceled")
        void 실패2() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer themeId = createTheme(managerToken, themeName);

            Integer reservationId = createReservationWithToken(memberToken, dateId, timeId,
                themeId);
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
        @DisplayName("cancel not past")
        @Sql(
            scripts = {"classpath:truncate.sql", "classpath:test-member.sql",
                "classpath:past-reservation.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        )
        void 실패3() {
            Long sqlSavedId = 1L;

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .when().patch("/member/reservations/" + sqlSavedId + "/cancel")
                .then().log().all()
                .statusCode(RESERVATION_ALREADY_PAST.getHttpStatus().value())
                .body("message", is(RESERVATION_ALREADY_PAST.getMessage()));
        }
    }

    @Nested
    @DisplayName("changeSchedule 메서드는")
    class ChangeScheduleTest {


        @Test
        @DisplayName("changeSchedule")
        void 성공() {
            String futureDate = LocalDate.now().plusDays(1).toString();
            String futureTime = LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS)
                .toString();
            Integer dateId = createReservationDate(managerToken, date);
            Integer changedDateId = createReservationDate(managerToken, futureDate);
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer changedTimeId = createReservationTime(managerToken, futureTime);
            Integer themeId = createTheme(managerToken, themeName);
            Integer reservationId = createReservationWithToken(memberToken, dateId, timeId,
                themeId);

            Map<String, Object> params = new HashMap<>();
            params.put("dateId", changedDateId);
            params.put("timeId", changedTimeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch(
                    "/member/reservations/" + reservationId + "/schedule?name=" + reservationName)
                .then().log().all()
                .statusCode(200)
                .body("date", is(futureDate))
                .body("time", is(futureTime));
        }


        @Test
        @DisplayName("changeSchedule not owner")
        void 실패1() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer changedDateId = createReservationDate(managerToken,
                LocalDate.now().plusDays(1).toString());
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer changedTimeId = createReservationTime(managerToken,
                LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());
            Integer themeId = createTheme(managerToken, themeName);
            Integer reservationId = createReservationWithToken(memberToken, dateId, timeId,
                themeId);

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
        @DisplayName("changeSchedule already canceled")
        void 실패2() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer changedDateId = createReservationDate(managerToken,
                LocalDate.now().plusDays(1).toString());
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer changedTimeId = createReservationTime(managerToken,
                LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());
            Integer themeId = createTheme(managerToken, themeName);
            Integer reservationId = createReservationWithToken(memberToken, dateId, timeId,
                themeId);
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
        @DisplayName("changeSchedule past")
        @Sql(
            scripts = {"classpath:truncate.sql", "classpath:test-member.sql",
                "classpath:past-reservation.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        )
        void 실패3() {
            Integer changedDateId = createReservationDate(managerToken,
                LocalDate.now().plusDays(1).toString());
            Integer changedTimeId = createReservationTime(managerToken,
                LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());

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
        @DisplayName("changeSchedule new datetime is past")
        @Sql(
            scripts = {"classpath:truncate.sql", "classpath:test-member.sql",
                "classpath:past-reservation-date.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        )
        void 실패4() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer pastDateId = 1;
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer changedTimeId = createReservationTime(managerToken,
                LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());
            Integer themeId = createTheme(managerToken, themeName);
            Integer reservationId = createReservationWithToken(memberToken, dateId, timeId,
                themeId);

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
    }
}
