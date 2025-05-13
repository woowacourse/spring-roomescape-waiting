package roomescape.theme.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ThemeRestControllerTest {

    @Autowired
    private ThemeRestController themeRestController;

    @Test
    void 테마를_추가한다() {
        final Map<String, String> params = new HashMap<>();
        params.put("name", "우가우가");
        params.put("description", "우가우가 설명");
        params.put("thumbnail", "따봉우가.jpg");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 테마를_조회한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 테마를_삭제한다() {
        final Map<String, String> params = new HashMap<>();
        params.put("name", "우가우가");
        params.put("description", "우가우가 설명");
        params.put("thumbnail", "따봉우가.jpg");

        final Integer id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/{id}", id)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
