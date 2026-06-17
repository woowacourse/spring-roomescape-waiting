package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.date.exception.ReservationDateErrorInformation.INACTIVE_DATE_NOT_ALLOWED;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.date.fixture.ReservationDateApiFixture.updateDateStatus;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_CANCELED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_PAST;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NOT_OWNER;
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
        @DisplayName("예약을 생성한다")
        void 성공1() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer themeId = createTheme(managerToken, themeName);
            createReservationWithToken(memberToken, dateId, timeId, themeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .when().get("/member/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
        }


        @Test
        @DisplayName("같은 이름으로 취소된 예약이 있어도 예약 생성이 가능하다")
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
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/reservations")
                .then().log().all()
                .statusCode(200);
        }


        @Test
        @DisplayName("다른 이름으로 취소된 예약이 있어도 예약 생성이 가능하다")
        void 성공3() {
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


        @Test
        @DisplayName("이미 예약한 적이 있으면 409을 반환한다")
        void 실패1() {
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
        @DisplayName("날짜 없이 예약 생성을 시도하면 400을 반환한다")
        void 실패2() {
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
        @DisplayName("시간 없이 예약 생성을 시도하면 400을 반환한다")
        void 실패3() {
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
        @DisplayName("테마 없이 예약 생성을 시도하면 400을 반환한다")
        void 실패4() {
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
        @DisplayName("비활성화된 날짜로 예약 생성을 시도하면 400을 반환한다")
        void 실패5() {
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
        @DisplayName("비활성화된 시간으로 예약 생성을 시도하면 400을 반환한다")
        void 실패6() {
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
        @DisplayName("비활성화된 테마로 예약 생성을 시도하면 400을 반환한다")
        void 실패7() {
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
    @DisplayName("getMyReservations 메서드는")
    class GetMyReservationsTest {


        @Test
        @DisplayName("나의 예약을 조회한다")
        void 성공1() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer themeId = createTheme(managerToken, themeName);

            Integer timeId = createReservationTime(managerToken, startAt);
            Integer otherTimeId = createReservationTime(managerToken, otherStartAt);

            createReservationWithToken(memberToken, dateId, timeId, themeId);
            createReservationWithToken(anotherToken, dateId, otherTimeId, themeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .when().get("/member/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, anotherToken)
                .when().get("/member/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
        }
    }

    @Nested
    @DisplayName("cancel 메서드는")
    class CancelTest {


        @Test
        @DisplayName("예약을 취소한다")
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
        @DisplayName("예약자가 아니면 403을 반환한다")
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
        @DisplayName("이미 취소된 예약이면 409를 반환한다")
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
        @DisplayName("과거 예약이면 409를 반환한다")
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
        @DisplayName("날짜 및 시간을 변경한다")
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
        @DisplayName("예약자가 아니면 403을 반환한다")
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
        @DisplayName("이미 취소된 예약이면 409를 반환한다")
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
        @DisplayName("과거 예약이면 409를 반환한다")
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
        @DisplayName("과거 날짜 및 시간으로 변경하려는 경우 400을 반환한다")
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
