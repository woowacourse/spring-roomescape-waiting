package roomescape.time.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import roomescape.common.AcceptanceTest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.reservation.fixture.ReservationApiFixture.createReservationWithToken;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;
import static roomescape.time.exception.ReservationTimeErrorInformation.START_AT_IS_NULL;
import static roomescape.time.fixture.ReservationTimeApiFixture.createReservationTime;
import static roomescape.time.fixture.ReservationTimeApiFixture.updateTimeStatus;

class ReservationTimeAdminControllerTest extends AcceptanceTest {

    private final String startAt1 = "10:00:00";
    private static String startAt2 = "11:00:00";

    @Test
    @DisplayName("관리자는 예약 시간 목록을 조회한다.")
    void get_reservation_times() {
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("관리자로 등록된 시간 조회시, 활성화/비활성화된 시간을 모두 조회한다.")
    void readAvailableTimesExcludeInactive() {
        Integer activeTimeId = createReservationTime(managerToken, startAt1);
        Integer inactiveTimeId = createReservationTime(managerToken, startAt2);
        updateTimeStatus(managerToken, activeTimeId, true);
        updateTimeStatus(managerToken, inactiveTimeId, false);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }


    @Test
    @DisplayName("관리자는 예약 시간을 생성한다.")
    void create_reservation_time() {
        Integer timeId = createReservationTime(managerToken, startAt1);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(timeId))
                .body("[0].startAt", is(startAt1));
    }

    @Test
    @DisplayName("startAt이 없으면 예약 시간 생성에 실패한다.")
    void create_reservation_time_without_start_at() {
        Map<String, Object> params = new HashMap<>();
        params.put("startAt", null);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(START_AT_IS_NULL.getHttpStatus().value())
                .body("message", is("요청 값 검증에 실패했습니다."));
    }

    @Test
    @DisplayName("이미 예약된 시간이어도 관리자가 비활성화할 수 있다.")
    void shouldThrowException_WhenDeleteDate_AboutAlreadyReserved() {
        Integer dateId = createReservationDate(managerToken, LocalDate.of(2099, 1, 1).toString());
        Integer timeId = createReservationTime(managerToken, "10:00");
        Integer themeId = createTheme(managerToken, "테마1");
        createReservationWithToken(managerToken, dateId, timeId, themeId);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("isActive", false);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(updateParams)
                .when().patch("/admin/times/" + timeId + "/status")
                .then().log().all()
                .statusCode(200);
    }

}
