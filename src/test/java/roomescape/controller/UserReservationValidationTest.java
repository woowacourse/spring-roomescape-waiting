package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.FixedClockConfig;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-waiting-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserReservationValidationTest {

    @Test
    @DisplayName("예약 변경 시 날짜가 없으면 400과 함께 date 필드 오류 메시지를 반환한다.")
    void updateWithNullDate() {
        Map<String, Object> params = new HashMap<>();
        params.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "user_b")
                .body(params)
                .when().patch("/reservations/2")
                .then().log().all()
                .statusCode(400)
                .body(containsString("date"));
    }

    @Test
    @DisplayName("예약 변경 시 날짜 형식이 잘못되면 400과 함께 date 필드 오류 메시지를 반환한다.")
    void updateWithInvalidDateFormat() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "05-01-2026");
        params.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "user_b")
                .body(params)
                .when().patch("/reservations/2")
                .then().log().all()
                .statusCode(400)
                .body(containsString("date"));
    }

    @Test
    @DisplayName("예약 변경 시 timeId가 0이면 400과 함께 timeId 필드 오류 메시지를 반환한다.")
    void updateWithZeroTimeId() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-07-01");
        params.put("timeId", 0L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "user_b")
                .body(params)
                .when().patch("/reservations/2")
                .then().log().all()
                .statusCode(400)
                .body(containsString("timeId"));
    }
}
