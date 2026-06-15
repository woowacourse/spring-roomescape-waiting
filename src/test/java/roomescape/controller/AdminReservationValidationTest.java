package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminReservationValidationTest {

    @Test
    @DisplayName("관리자 예약 생성 시 이름이 없으면 400과 함께 name 필드 오류 메시지를 반환한다.")
    void createReservationWithBlankName() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("name"));
    }

    @Test
    @DisplayName("관리자 예약 생성 시 날짜가 없으면 400과 함께 date 필드 오류 메시지를 반환한다.")
    void createReservationWithNullDate() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("date"));
    }

    @Test
    @DisplayName("관리자 예약 생성 시 날짜 형식이 잘못되면 400과 함께 date 필드 오류 메시지를 반환한다.")
    void createReservationWithInvalidDateFormat() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "06-05-2026");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("date"));
    }

    @Test
    @DisplayName("관리자 예약 생성 시 timeId가 0이면 400과 함께 timeId 필드 오류 메시지를 반환한다.")
    void createReservationWithZeroTimeId() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-06-05");
        params.put("timeId", 0L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("timeId"));
    }

    @Test
    @DisplayName("관리자 예약 생성 시 themeId가 0이면 400과 함께 themeId 필드 오류 메시지를 반환한다.")
    void createReservationWithZeroThemeId() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 0L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("themeId"));
    }

    @Test
    @DisplayName("관리자 예약 생성 시 존재하지 않는 timeId면 400을 반환한다.")
    void createReservationWithNonExistentTimeId() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-06-05");
        params.put("timeId", 999L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("관리자 예약 생성 시 존재하지 않는 themeId면 400을 반환한다.")
    void createReservationWithNonExistentThemeId() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 999L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("관리자 예약 삭제 시 id가 숫자가 아니면 400을 반환한다.")
    void deleteReservationWithInvalidId() {
        RestAssured.given().log().all()
                .when().delete("/admin/reservations/abc")
                .then().log().all()
                .statusCode(400);
    }
}
