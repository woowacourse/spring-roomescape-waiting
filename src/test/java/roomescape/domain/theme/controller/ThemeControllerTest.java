package roomescape.domain.theme.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.ControllerTest;
import roomescape.domain.theme.dto.ThemeAddRequest;

import static org.hamcrest.Matchers.is;

class ThemeControllerTest extends ControllerTest {

    @DisplayName("전체 테마를 조회할 수 있다 (200 OK)")
    @Test
    void should_get_theme_list() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }

    @DisplayName("테마를 추가할 수 있다 (201 created)")
    @Test
    void should_add_theme() {
        ThemeAddRequest themeAddRequest = new ThemeAddRequest("도도", "배고픔", "url");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeAddRequest)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("테마를 삭제할 수 있다 (204 no content)")
    @Test
    void should_remove_theme() {
        RestAssured.given().log().all()
                .when().delete("/themes/5")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("인기 테마 목록을 불러올 수 있다.(200 OK)")
    @Test
    void should_response_theme_ranking() {
        RestAssured.given().log().all()
                .when().get("/theme/ranking")
                .then().log().all()
                .statusCode(200);
    }
}
