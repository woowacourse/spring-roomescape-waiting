package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ThemeControllerTest {

    @Test
    @DisplayName("/themes 요청 시 테마 조회")
    void readThemes() {
        RestAssured.given().log().all()
            .when().get("/themes")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("테마 관리 페이지 내에서 테마 추가")
    void createTheme() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(getTestParamsWithTheme())
            .when().post("/themes")
            .then().log().all()
            .statusCode(201);

        RestAssured.given().log().all()
            .when().get("/themes")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("테마 관리 페이지 내에서 테마 삭제")
    void deleteTheme() {
        RestAssured.given().log().all()
            .when().delete("/themes/1")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .when().get("/themes")
            .then().log().all()
            .statusCode(200);
    }

    private Map<String, String> getTestParamsWithTheme() {
        return Map.of(
            "name", "이름",
            "description", "설명",
            "thumbnail", "썸네일"
        );
    }
}
