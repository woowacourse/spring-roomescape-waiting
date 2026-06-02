package roomescape.acceptance.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;

public class ThemeFixture {

    public static void createTheme(String name, String description, String thumbnailUrl) {
        Map<String, String> theme = new HashMap<>();
        theme.put("name", name);
        theme.put("description", description);
        theme.put("thumbnailUrl", thumbnailUrl);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    public static void deleteTheme(Long id) {
        RestAssured.given().log().all()
                .when().delete("/admin/themes/" + id)
                .then().log().all()
                .statusCode(204);
    }
}