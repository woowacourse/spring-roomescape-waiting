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
class AdminThemeValidationTest {

    @Test
    @DisplayName("테마 생성 시 이름이 없으면 400과 함께 name 필드 오류 메시지를 반환한다.")
    void createThemeWithBlankName() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("thumbnailUrl", "https://example.com/img.jpg");
        params.put("description", "설명");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body(containsString("name"));
    }

    @Test
    @DisplayName("테마 생성 시 이름이 공백만 있으면 400과 함께 name 필드 오류 메시지를 반환한다.")
    void createThemeWithWhitespaceName() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "   ");
        params.put("thumbnailUrl", "https://example.com/img.jpg");
        params.put("description", "설명");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body(containsString("name"));
    }

    @Test
    @DisplayName("테마 생성 시 thumbnailUrl과 description이 없어도 성공한다.")
    void createThemeWithoutOptionalFields() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "공포의 방");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }
}
