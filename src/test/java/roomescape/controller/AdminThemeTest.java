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

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-waiting-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminThemeTest {

    @Test
    @DisplayName("테마를 생성하는지에 대한 테스트")
    void createTheme() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "공포");
        params.put("thumbnailUrl", "test_url");
        params.put("description", "공포_설명");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("name", is("공포"))
                .body("thumbnailUrl", is("test_url"))
                .body("description", is("공포_설명"));
    }

    @Test
    @DisplayName("테마를 조회하는지에 대한 테스트")
    void readThemes() {
        RestAssured.given().log().all()
                .when().get("/admin/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("예약 없는 테마 삭제 성공")
    void deleteThemeWithoutReservation() {
        RestAssured.given().log().all()
                .when().delete("/admin/themes/2")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("예약 있는 테마 삭제 실패")
    void deleteThemeWithReservation() {
        RestAssured.given().log().all()
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(400);
    }
}
