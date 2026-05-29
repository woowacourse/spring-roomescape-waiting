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

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-waiting-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminTimeTest {
    @Test
    @DisplayName("테마를 생성하는지에 대한 테스트")
    void createTheme() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "11:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", is("11:00"));
    }

    @Test
    @DisplayName("시간을 조회하는지에 대한 테스트")
    void readThemes() {
        RestAssured.given().log().all()
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }


    @Test
    @DisplayName("예약 없는 시간 삭제 성공")
    void deleteThemeWithoutReservation() {
        RestAssured.given().log().all()
                .when().delete("/admin/times/3")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("예약 있는 시간 삭제 실패")
    void deleteThemeWithReservation() {
        RestAssured.given().log().all()
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(400);
    }
}
