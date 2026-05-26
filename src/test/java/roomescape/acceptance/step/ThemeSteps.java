package roomescape.acceptance.step;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;

public class ThemeSteps {

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

    public static void checkAllThemeSize(int expectedSize) {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(expectedSize));
    }

    public static void deleteTheme(Long id) {
        RestAssured.given().log().all()
                .when().delete("/admin/themes/" + id)
                .then().log().all()
                .statusCode(204);
    }

    public static void checkThemeRanking(String startDate, String endDate, int expectedRanking) {
        RestAssured.given().log().all()
                .param("start-date", startDate)
                .param("end-date", endDate)
                .when().get("/themes/ranking")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", is(expectedRanking));
    }
}
