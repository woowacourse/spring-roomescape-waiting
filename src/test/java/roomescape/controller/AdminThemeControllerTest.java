package roomescape.controller;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminThemeControllerTest extends ControllerTest {

    private static final String THEME_NAME = "공포의 폐병원";
    private static final String THEME_DESCRIPTION = "공포의 폐병원";
    private static final String THEME_THUMBNAIL_URL =
            "https://images.unsplash.com/photo-1505635552518-3448ff116af3?w=300&q=80";

    @DisplayName("관리자 테마 추가")
    @Test
    void 관리자_테마_추가() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams(THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_URL))
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("관리자 테마 삭제")
    @Test
    void API_관리자_테마_삭제() {
        long id = createTheme(THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_URL);

        RestAssured.given().log().all()
                .pathParam("id", id)
                .when().delete("/admin/themes/{id}")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 테마 삭제하면 404")
    @Test
    void 존재하지_않는_테마_삭제하면_404() {
        RestAssured.given().log().all()
                .pathParam("id", 999)
                .when().delete("/admin/themes/{id}")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 테마입니다."));
    }

    @DisplayName("예약 또는 대기에 사용 중인 테마 삭제하면 409")
    @Test
    void 예약에_사용중인_테마_삭제하면_400() {
        RestAssured.given().log().all()
                .pathParam("id", 1)
                .when().delete("/admin/themes/{id}")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("예약 또는 대기에 사용 중인 테마는 삭제할 수 없습니다."));
    }

    private long createTheme(String name, String description, String thumbnailUrl) {
        String location = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams(name, description, thumbnailUrl))
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .header("Location");

        return Long.parseLong(location.split("/")[2]);
    }

    private Map<String, Object> themeParams(String name, String description, String thumbnailUrl) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("description", description);
        params.put("thumbnailUrl", thumbnailUrl);
        return params;
    }
}
