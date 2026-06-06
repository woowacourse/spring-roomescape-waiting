package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.reservation.exception.ReservationErrorInformation.*;
import static roomescape.reservation.fixture.ReservationApiFixture.*;
import static roomescape.slot.fixture.SlotApiFixture.createSlot;
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
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.AcceptanceTest;

class ReservationAdminControllerTest extends AcceptanceTest {

    private final String date = LocalDate.of(2099, 1, 1).toString();
    private final String startAt = "11:00";
    private final String themeName = "테마1";

    @Test
    @DisplayName("관리자는 전체 예약 목록을 조회한다.")
    void get_reservations() {
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("관리자는 예약을 생성한다.")
    void reserve_reservation() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        createReservationWithToken(managerToken, slotId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("관리자는 예약을 취소할 수 있다.")
    void cancelByManager_reservation() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer reservationId = createReservationWithToken(managerToken, slotId);

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

    @Test
    @DisplayName("dateId가 없으면 예약 생성에 실패한다.")
    void reserve_reservation_without_date_id() {
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
    @DisplayName("timeId가 없으면 예약 생성에 실패한다.")
    void reserve_reservation_without_time_id() {
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
    @DisplayName("themeId가 없으면 예약 생성에 실패한다.")
    void reserve_reservation_without_theme_id() {
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

    @Test
    @DisplayName("관리자는 예약자 확인 없이, 예약 날짜/시간을 변경할 수 있다.")
    void updateSchedule() {
        String futureDate = LocalDate.now().plusDays(1).toString();
        String futureTime = LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString();
        Integer dateId = createReservationDate(managerToken, date);
        Integer changedDateId = createReservationDate(managerToken, futureDate);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer changedTimeId = createReservationTime(managerToken, futureTime);
        Integer themeId = createTheme(managerToken, themeName);
        createSlot(managerToken, changedDateId, changedTimeId, themeId);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer reservationId = createReservationWithToken(managerToken, slotId);

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

    @Test
    @DisplayName("이미 취소된 예약을 변경하면 예외가 발생한다.")
    void updateScheduleByManager_already_canceled() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer changedDateId = createReservationDate(managerToken, LocalDate.now().plusDays(1).toString());
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer changedTimeId = createReservationTime(managerToken, LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());
        Integer themeId = createTheme(managerToken, themeName);
        createSlot(managerToken, changedDateId, changedTimeId, themeId);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer reservationId = createReservationWithToken(managerToken, slotId);

        cancelReservationWithToken(this.managerToken, reservationId);

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
    @DisplayName("관리자가 이미 존재하는 날짜/시간으로 예약을 변경하면 예외가 발생한다.")
    void updateScheduleByManager_fail_duplicated() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer alreadyReservedDateId = createReservationDate(managerToken, LocalDate.now().plusDays(1).toString());
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer alreadyReservedTimeId = createReservationTime(managerToken, LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer reservationId = createReservationWithToken(managerToken, slotId);
        Integer alreadyReservedSlotId = createSlot(managerToken, alreadyReservedDateId, alreadyReservedTimeId, themeId);
        createReservationWithToken(managerToken, alreadyReservedSlotId);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", alreadyReservedDateId);
        params.put("timeId", alreadyReservedTimeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/admin/reservations/" + reservationId + "/schedule")
                .then().log().all()
                .statusCode(RESERVATION_ALREADY_BOOKED.getHttpStatus().value())
                .body("message", is(RESERVATION_ALREADY_BOOKED.getMessage()));
    }

    @Test
    @DisplayName("관리자가 예약을 과거의 날짜/시간으로 변경하면 예외가 발생한다.")
    @Sql(
            scripts = {"classpath:truncate.sql", "classpath:test-member.sql", "classpath:past-reservation-date.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    void updateScheduleByManager_pastDateTime() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer pastSqlDateId = 1;
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer pastTimeId = createReservationTime(managerToken, "00:01");
        Integer themeId = createTheme(managerToken, themeName);
        createSlot(managerToken, pastSqlDateId, pastTimeId, themeId);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer reservationId = createReservationWithToken(managerToken, slotId);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", pastSqlDateId);
        params.put("timeId", pastTimeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/admin/reservations/" + reservationId + "/schedule")
                .then().log().all()
                .statusCode(RESERVATION_ALREADY_PAST.getHttpStatus().value())
                .body("message", is(RESERVATION_ALREADY_PAST.getMessage()));
    }

}
