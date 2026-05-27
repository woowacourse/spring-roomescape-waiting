package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.reservation.exception.ReservationErrorInformation.*;
import static roomescape.reservation.fixture.ReservationApiFixture.*;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;
import static roomescape.time.fixture.ReservationTimeApiFixture.createReservationTime;

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

class ReservationAdminControllerTest extends AcceptanceTest {

    private final String date = LocalDate.of(2099, 1, 1).toString();
    private final String startAt = "11:00";
    private final String themeName = "테마1";


    @Nested
    @DisplayName("getReservations 메서드는")
    class GetTest {


        @Test
        @DisplayName("모든 예약을 조회한다")
        void 성공() {
            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
        }
    }

    @Nested
    @DisplayName("create 메서드는")
    class CreateTest {


        @Test
        @DisplayName("예약을 생성한다")
        void 성공() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer themeId = createTheme(managerToken, themeName);

            createReservationWithToken(managerToken, dateId, timeId, themeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
        }


        @Test
        @DisplayName("요청 본문에 date id가 없으면 400을 반환한다")
        void 실패1() {
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer themeId = createTheme(managerToken, themeName);

            Map<String, Object> params = new HashMap<>();
            params.put("dateId", null);
            params.put("timeId", timeId);
            params.put("themeId", themeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", is("요청 값 검증에 실패했습니다."));
        }


        @Test
        @DisplayName("요청 본문에 time id가 없으면 400을 반환한다")
        void 실패2() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer themeId = createTheme(managerToken, themeName);

            Map<String, Object> params = new HashMap<>();
            params.put("dateId", dateId);
            params.put("timeId", null);
            params.put("themeId", themeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", is("요청 값 검증에 실패했습니다."));
        }


        @Test
        @DisplayName("요청 본문에 theme id가 없으면 400을 반환한다")
        void 실패3() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer timeId = createReservationTime(managerToken, startAt);

            Map<String, Object> params = new HashMap<>();
            params.put("dateId", dateId);
            params.put("timeId", timeId);
            params.put("themeId", null);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", is("요청 값 검증에 실패했습니다."));
        }
    }

    @Nested
    @DisplayName("cancelReservation 메서드는")
    class CancelByManagerTest {


        @Test
        @DisplayName("예약을 취소한다")
        void 성공() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer themeId = createTheme(managerToken, themeName);

            Integer reservationId = createReservationWithToken(managerToken, dateId, timeId,
                themeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().patch("/admin/reservations/" + reservationId + "/cancel")
                .then().log().all()
                .statusCode(200);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
        }
    }

    @Nested
    @DisplayName("updateSchedule 메서드는")
    class UpdateScheduleTest {


        @Test
        @DisplayName("예약 정보를 변경한다")
        void 성공() {
            String futureDate = LocalDate.now().plusDays(1).toString();
            String futureTime = LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS)
                .toString();
            Integer dateId = createReservationDate(managerToken, date);
            Integer changedDateId = createReservationDate(managerToken, futureDate);
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer changedTimeId = createReservationTime(managerToken, futureTime);
            Integer themeId = createTheme(managerToken, themeName);
            Integer reservationId = createReservationWithToken(managerToken, dateId, timeId,
                themeId);

            Map<String, Object> params = new HashMap<>();
            params.put("dateId", changedDateId);
            params.put("timeId", changedTimeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/admin/reservations/" + reservationId + "/schedule")
                .then().log().all()
                .statusCode(200)
                .body("date", is(futureDate))
                .body("time", is(futureTime));
        }
    }

    @Nested
    @DisplayName("updateScheduleByManager 메서드는")
    class UpdateScheduleByManagerTest {


        @Test
        @DisplayName("updateScheduleByManager already canceled")
        void 실패1() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer changedDateId = createReservationDate(managerToken,
                LocalDate.now().plusDays(1).toString());
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer changedTimeId = createReservationTime(managerToken,
                LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());
            Integer themeId = createTheme(managerToken, themeName);
            Integer reservationId = createReservationWithToken(managerToken, dateId, timeId,
                themeId);

            cancelReservationWithToken(ReservationAdminControllerTest.this.managerToken,
                reservationId);

            Map<String, Object> params = new HashMap<>();
            params.put("dateId", changedDateId);
            params.put("timeId", changedTimeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/admin/reservations/" + reservationId + "/schedule")
                .then().log().all()
                .statusCode(RESERVATION_ALREADY_CANCELED.getHttpStatus().value())
                .body("message", is(RESERVATION_ALREADY_CANCELED.getMessage()));
        }


        @Test
        @DisplayName("updateScheduleByManager duplicated")
        void 실패2() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer alreadyReservedDateId = createReservationDate(managerToken,
                LocalDate.now().plusDays(1).toString());
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer alreadyReservedTimeId = createReservationTime(managerToken,
                LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());
            Integer themeId = createTheme(managerToken, themeName);
            Integer reservationId = createReservationWithToken(managerToken, dateId, timeId,
                themeId);
            createReservationWithToken(managerToken, alreadyReservedDateId, alreadyReservedTimeId,
                themeId);

            Map<String, Object> params = new HashMap<>();
            params.put("dateId", alreadyReservedDateId);
            params.put("timeId", alreadyReservedTimeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/admin/reservations/" + reservationId + "/schedule")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
        }


        @Test
        @DisplayName("updateScheduleByManager pastDateTime")
        @Sql(
            scripts = {"classpath:truncate.sql", "classpath:test-member.sql",
                "classpath:past-reservation-date.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        )
        void 실패3() {
            Integer dateId = createReservationDate(managerToken, date);
            Integer pastSqlDateId = 1;
            Integer timeId = createReservationTime(managerToken, startAt);
            Integer pastTimeId = createReservationTime(managerToken, "00:01");
            Integer themeId = createTheme(managerToken, themeName);
            Integer reservationId = createReservationWithToken(managerToken, dateId, timeId,
                themeId);

            Map<String, Object> params = new HashMap<>();
            params.put("dateId", pastSqlDateId);
            params.put("timeId", pastTimeId);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/admin/reservations/" + reservationId + "/schedule")
                .then().log().all()
                .statusCode(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getHttpStatus().value())
                .body("message", is(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getMessage()));
        }
    }
}
