package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import roomescape.AcceptanceTest;

public class AdminThemeControllerTest extends AcceptanceTest {

    @Test
    void 테마를_추가한다() {
        Map<String, String> themeParams = new HashMap<>();
        themeParams.put("name", "방탈출1");
        themeParams.put("description", "다함께 탈출해요 방탈출.");
        themeParams.put("thumbnail", "https://asdfsdf.sdfs");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 테마를_삭제한다() {
        Map<String, String> themeParams = new HashMap<>();
        themeParams.put("name", "방탈출11");
        themeParams.put("description", "다함께 탈출해요 방탈출.");
        themeParams.put("thumbnail", "https://asdfsdf.sdfs");

        int themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        RestAssured.given().log().all()
                .when().delete("/admin/themes/" + themeId)
                .then().log().all()
                .statusCode(204);
    }
}
