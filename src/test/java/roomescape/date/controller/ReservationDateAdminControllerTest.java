package roomescape.date.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_ALREADY_EXISTS;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_IS_NULL;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.date.fixture.ReservationDateApiFixture.updateDateStatus;
import static roomescape.reservation.fixture.ReservationApiFixture.createReservationWithToken;
import static roomescape.slot.fixture.SlotApiFixture.createSlot;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;
import static roomescape.time.fixture.ReservationTimeApiFixture.createReservationTime;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.AcceptanceTest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

class ReservationDateAdminControllerTest extends AcceptanceTest {

    private final String date = LocalDate.of(2099, 1, 1).toString();

    @Test
    @DisplayName("예약 날짜 목록을 조회한다.")
    void get_reservation_dates() {
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/dates")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("예약 날짜를 생성한다.")
    void reserve_reservation_date() {
        Map<String, String> params = new HashMap<>();
        params.put("date", date);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/dates")
                .then().log().all()
                .statusCode(200)
                .body("date", is(date));

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/dates")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("이미 등록된 날짜를 또 등록하면 예외가 발생한다.")
    void reserve_duplicated_date() {
        createReservationDate(managerToken, date);

        Map<String, String> params = new HashMap<>();
        params.put("date", date);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/dates")
                .then().log().all()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", is(DATE_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("예약 날짜를 생성한 뒤 조회한다.")
    void reserveAndGetReservationDates() {
        Map<String, String> params = new HashMap<>();
        params.put("date", date);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/dates")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/dates")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("date가 없으면 예약 날짜 생성에 실패한다.")
    void reserve_reservation_date_without_date() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", null);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/dates")
                .then().log().all()
                .statusCode(DATE_IS_NULL.getHttpStatus().value())
                .body("message", is("요청 값 검증에 실패했습니다."));
    }

    @Test
    @DisplayName("관리자는 활성화/비활성화된 날짜 목록을 조회한다.")
    @Sql(
            scripts = "classpath:past-reservation-date.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    void getReservationDatesAfterToday() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        Integer id = createReservationDate(managerToken, tomorrow);
        updateDateStatus(managerToken, id, true);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/dates")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("예약이 이미 된 날짜를 관리자가 비활성화할 수 있다.")
    void shouldThrowException_WhenDeleteDate_AboutAlreadyReserved() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, "10:00");
        Integer themeId = createTheme(managerToken, "테마1");
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);
        createReservationWithToken(managerToken, slotId);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("isActive", false);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(updateParams)
                .when().patch("/admin/dates/" + dateId + "/status")
                .then().log().all()
                .statusCode(200);
    }

}
