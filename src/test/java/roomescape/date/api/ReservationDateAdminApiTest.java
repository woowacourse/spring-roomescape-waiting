package roomescape.date.api;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_ALREADY_EXISTS;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_IS_NULL;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.date.fixture.ReservationDateApiFixture.updateDateStatus;
import static roomescape.reservation.fixture.ReservationApiFixture.createReservationWithToken;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;
import static roomescape.time.fixture.ReservationTimeApiFixture.createReservationTime;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.AcceptanceTest;

class ReservationDateAdminApiTest extends AcceptanceTest {

    private final String date = LocalDate.of(2099, 1, 1).toString();


    @Test
    @DisplayName("관리자는 모든 날짜 목록을 조회할 수 있다")
    void 관리자는_모든_예약_목록을_조회할_수_있다() {
        // given
        createReservationDate(managerToken, date);

        // when & then
        RestAssured.given().log().all()
            .header(HttpHeaders.AUTHORIZATION, managerToken)
            .when().get("/admin/dates")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(1))
            .body("date", hasItem(date.toString()));
    }

    @Test
    @DisplayName("미래 예약 목록만 조회한다")
    @Sql(
        scripts = "classpath:past-reservation-date.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    void 미래_예약_목록만_조회한다() {
        // given
        String tomorrow = LocalDate.now().plusDays(1).toString();
        Integer id = createReservationDate(managerToken, tomorrow);
        updateDateStatus(managerToken, id, true);

        // when & then
        RestAssured.given().log().all()
            .header(HttpHeaders.AUTHORIZATION, managerToken)
            .when().get("/admin/dates")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(2));
    }


    @Test
    @DisplayName("예약 날짜를 생성한다")
    void 예약_날짜를_생성한다() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("date", date);

        // when
        RestAssured.given().log().all()
            .header(HttpHeaders.AUTHORIZATION, managerToken)
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/dates")
            .then().log().all()
            .statusCode(200)
            .body("date", is(date));

        // then
        RestAssured.given().log().all()
            .header(HttpHeaders.AUTHORIZATION, managerToken)
            .when().get("/admin/dates")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(1));
    }


    @Test
    @DisplayName("이미 등록된 날짜로 재등록을 시도하면 예외가 발생한다")
    void 이미_등록된_날짜로_재등록을_시도하면_예외가_발생한다() {
        // given
        createReservationDate(managerToken, date);

        // when
        Map<String, String> params = new HashMap<>();
        params.put("date", date);

        // then
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
    @DisplayName("요청 본문에 날짜가 없는 경우 예외가 발생한다")
    void 요청_본문에_날짜가_없는_경우_예외가_발생한다() {
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
    @DisplayName("예약이 이미 된 날짜를 관리자가 비활성화할 수 있다")
    void 예약이_이미_된_날짜를_관리자가_비활성화할_수_있다() {
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, "10:00");
        Integer themeId = createTheme(managerToken, "테마1");

        createReservationWithToken(managerToken, dateId, timeId, themeId);

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
