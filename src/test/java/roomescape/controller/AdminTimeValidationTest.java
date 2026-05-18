package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(FixedClockConfig.class)
@Sql(scripts = "/testReservationData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminTimeValidationTest {

    @Test
    @DisplayName("시간 생성 시 startAt이 없으면 400과 함께 startAt 필드 오류 메시지를 반환한다.")
    void createTimeWithNullStartAt() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Collections.emptyMap())
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body(containsString("startAt"));
    }

    @Test
    @DisplayName("시간 생성 시 startAt 형식이 잘못되면 400과 함께 startAt 필드 오류 메시지를 반환한다.")
    void createTimeWithInvalidStartAtFormat() {
        Map<String, Object> params = new HashMap<>();
        params.put("startAt", "25:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body(containsString("startAt"));
    }
}
