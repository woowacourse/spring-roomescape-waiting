package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.reservation.exception.ReservationErrorInformation.*;
import static roomescape.reservation.fixture.ReservationApiFixture.cancelReservationWithToken;
import static roomescape.reservation.fixture.ReservationApiFixture.createReservationWithToken;
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
import roomescape.reservation.domain.ReservationStatus;

class ReservationControllerTest extends AcceptanceTest {

    private final String reservationName = "브라운";

    private final String date = LocalDate.of(2099, 1, 1).toString();
    private final String startAt = "10:00";
    private final String otherStartAt = "11:00";

    private final String themeName = "테마1";

    @Test
    @DisplayName("사용자는 예약을 생성한다.")
    void reserve_reservation() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        createReservationWithToken(memberToken, slotId);

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
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer otherSlotId = createSlot(managerToken, dateId, otherTimeId, themeId);
        createReservationWithToken(memberToken, slotId);
        createReservationWithToken(anotherToken, otherSlotId);

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
    @DisplayName("빈 슬롯에 예약하면 결제 대기 상태가 된다.")
    void reserve_pending_payment() {
        // given
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);

        // when & then
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .when().post("/member/slots/" + slotId + "/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("status", is(ReservationStatus.PENDING_PAYMENT.name()));
    }

    @Test
    @DisplayName("다른 사람이 예약한 날짜/시간/테마를 예약하면 대기 상태된다.")
    void waited_duplicated_reserved() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        createReservationWithToken(managerToken, slotId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .when().post("/member/slots/" + slotId + "/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("status", is(ReservationStatus.WAITING.name()));
    }

    @Test
    @DisplayName("취소된 예약을 동일한 사람이 새롭게 예약할 수 있다.")
    void reserved_when_canceled_same_name() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer reservationId = createReservationWithToken(memberToken, slotId);
        cancelReservationWithToken(memberToken, reservationId, slotId);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/member/slots/" + slotId + "/reservations")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("취소된 예약을 동일한 사람이 새롭게 예약할 수 있다.")
    void reserved_when_canceled_another_name() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer reservationId = createReservationWithToken(memberToken, slotId);
        cancelReservationWithToken(memberToken, reservationId, slotId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, anotherToken)
                .contentType(ContentType.JSON)
                .when().post("/member/slots/" + slotId + "/reservations")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("사용자는 자신의 예약을 취소한다.")
    void cancel() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer reservationId = createReservationWithToken(memberToken, slotId);

        Map<String, String> params = new HashMap<>();
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/member/slots/" + slotId + "/reservations/" + reservationId + "/cancel")
                .then().log().all()
                .statusCode(200)
                .body("status", is("CANCELED"));
    }

    @Test
    @DisplayName("슬롯에 본인 예약이 없는 사람이 취소하면 예외가 발생한다.")
    void cancel_no_reservation_in_slot() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer reservationId = createReservationWithToken(memberToken, slotId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, anotherToken)
                .contentType(ContentType.JSON)
                .when().patch("/member/slots/" + slotId + "/reservations/" + reservationId + "/cancel")
                .then().log().all()
                .statusCode(RESERVATION_NOT_OWNER.getHttpStatus().value())
                .body("message", is(RESERVATION_NOT_OWNER.getMessage()));
    }

    @Test
    @DisplayName("활성 예약 없는 슬롯을 취소하면 예외가 발생한다.")
    void cancel_already_canceled() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer reservationId = createReservationWithToken(memberToken, slotId);
        cancelReservationWithToken(memberToken, reservationId, slotId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .when().patch("/member/slots/" + slotId + "/reservations/" + reservationId + "/cancel")
                .then().log().all()
                .statusCode(RESERVATION_NOT_FOUND.getHttpStatus().value())
                .body("message", is(RESERVATION_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("이미 지난 예약을 취소하면 예외가 발생한다.")
    @Sql(
            scripts = {"classpath:truncate.sql", "classpath:test-member.sql", "classpath:past-reservation.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    void cancel_not_past() {
        Long sqlSlotId = 1L;   // past-reservation.sql 에서 생성한 slot_id
        Long reservationId = 1L;

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .when().patch("/member/slots/" + sqlSlotId + "/reservations/" + reservationId + "/cancel")
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
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer newSlotId = createSlot(managerToken, changedDateId, changedTimeId, themeId);
        Integer reservationId = createReservationWithToken(memberToken, slotId);

        Map<String, Object> params = new HashMap<>();
        params.put("newSlotId", newSlotId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/member/slots/" + slotId + "/reservations/" + reservationId + "/reschedule")
                .then().log().all()
                .statusCode(200)
                .body("slotId", is(newSlotId));
    }

    @Test
    @DisplayName("본인의 예약이 아닌데 변경을 시도하면 예외가 발생한다.")
    void changeSchedule_not_owner() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer changedDateId = createReservationDate(managerToken, LocalDate.now().plusDays(1).toString());
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer changedTimeId = createReservationTime(managerToken, LocalTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS).toString());
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer newSlotId = createSlot(managerToken, changedDateId, changedTimeId, themeId);
        Integer reservationId = createReservationWithToken(memberToken, slotId);

        Map<String, Object> params = new HashMap<>();
        params.put("newSlotId", newSlotId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, anotherToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/member/slots/" + slotId + "/reservations/" + reservationId + "/reschedule")
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
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        Integer newSlotId = createSlot(managerToken, changedDateId, changedTimeId, themeId);
        Integer reservationId = createReservationWithToken(memberToken, slotId);
        cancelReservationWithToken(memberToken, reservationId, slotId);

        Map<String, Object> params = new HashMap<>();
        params.put("newSlotId", newSlotId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/member/slots/" + slotId + "/reservations/" + reservationId + "/reschedule")
                .then().log().all()
                .statusCode(RESERVATION_NOT_FOUND.getHttpStatus().value())
                .body("message", is(RESERVATION_NOT_FOUND.getMessage()));
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
        Integer sqlThemeId = 1;
        Integer newSlotId = createSlot(managerToken, changedDateId, changedTimeId, sqlThemeId);

        Long sqlSlotId = 1L;
        Long reservationId = 1L;

        Map<String, Object> params = new HashMap<>();
        params.put("newSlotId", newSlotId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/member/slots/" + sqlSlotId + "/reservations/" + reservationId + "/reschedule")
                .then().log().all()
                .statusCode(RESERVATION_ALREADY_PAST.getHttpStatus().value())
                .body("message", is(RESERVATION_ALREADY_PAST.getMessage()));
    }

}
