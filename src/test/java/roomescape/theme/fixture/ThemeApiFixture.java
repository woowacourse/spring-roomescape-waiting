package roomescape.theme.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

public class ThemeApiFixture {

    private static final String THEME_DESCRIPTION = "테마1 설명";
    private static final String THUMBNAIL_URL = "테마1 썸네일";
    private static final Long DEFAULT_AMOUNT = 1000L;

    private ThemeApiFixture() {
    }

    public static Integer createTheme(String token, String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("description", THEME_DESCRIPTION);
        params.put("thumbnailUrl", THUMBNAIL_URL);
        params.put("amount", DEFAULT_AMOUNT);

        return RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("id");
    }

    public static void updateThemeStatus(String token, Integer themeId, boolean isActive) {
        Map<String, Object> updateActive = new HashMap<>();
        updateActive.put("isActive", isActive);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(updateActive)
                .when().patch("/admin/themes/" + themeId)
                .then().log().all()
                .statusCode(200);
    }

}
