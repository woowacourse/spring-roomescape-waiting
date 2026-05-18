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
@Sql(scripts = "/testReservationData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(FixedClockConfig.class)
public class ReservationValidationTest {

    @Test
    @DisplayName("이름이 빈 문자열이면 400과 함께 name 필드 오류 메시지를 반환한다.")
    void emptyNameReservationTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("name"));
    }

    @Test
    @DisplayName("이름이 공백만 있으면 400과 함께 name 필드 오류 메시지를 반환한다.")
    void blankNameReservationTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "   ");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("name"));
    }

    @Test
    @DisplayName("날짜가 없으면 400과 함께 date 필드 오류 메시지를 반환한다.")
    void nullDateReservationTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("date"));
    }

    @Test
    @DisplayName("날짜가 yyyy-MM-dd 형식이 아니면 400과 함께 date 필드 오류 메시지를 반환한다.")
    void invalidDateFormatReservationTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "06-05-2026");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("date"));
    }

    @Test
    @DisplayName("존재하지 않는 날짜이면 400과 함께 date 필드 오류 메시지를 반환한다.")
    void invalidDateReservationTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-13-05");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("date"));
    }

    @Test
    @DisplayName("timeId가 0이면 400과 함께 timeId 필드 오류 메시지를 반환한다.")
    void zeroTimeIdReservationTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-06-05");
        params.put("timeId", 0L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("timeId"));
    }

    @Test
    @DisplayName("themeId가 0이면 400과 함께 themeId 필드 오류 메시지를 반환한다.")
    void zeroThemeIdReservationTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 0L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("themeId"));
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 예약 시 400을 반환한다.")
    void nonExistentTimeIdReservationTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-06-05");
        params.put("timeId", 999L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("존재하지 않는 themeId로 예약 시 400을 반환한다.")
    void nonExistentThemeIdReservationTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 999L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
