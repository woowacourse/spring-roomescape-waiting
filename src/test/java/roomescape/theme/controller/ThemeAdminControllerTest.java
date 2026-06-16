package roomescape.theme.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.theme.exception.ThemeErrorInformation.*;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import roomescape.common.AcceptanceTest;

class ThemeAdminControllerTest extends AcceptanceTest {

    private final String themeName = "테마1";
    private final String themeDescription = "테마1 설명";
    private final String thumbnailUrl = "테마1 썸네일";
    private final String defaultThumbnailUrl = "DEFAULT_THUMBNAIL_URL";
    private final Long amount = 1000L;

    @Test
    @DisplayName("관리자는 테마 목록을 조회한다.")
    void get_themes() {
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("관리자는 테마를 생성한다.")
    void create_theme() {
        createTheme(managerToken, themeName);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("관리자는 가격을 포함하여 테마를 생성한다.")
    void create_theme_returns_amount() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", themeName);
        params.put("description", themeDescription);
        params.put("thumbnailUrl", thumbnailUrl);
        params.put("amount", amount);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(200)
                .body("amount", is(1000));
    }

    @Test
    @DisplayName("관리자는 테마 활성화 상태를 변경한다.")
    void update_theme_status() {
        Integer themeId = createTheme(managerToken, themeName);

        Map<String, Boolean> params = new HashMap<>();
        params.put("isActive", true);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/admin/themes/" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("id", is(themeId))
                .body("isActive", is(true));
    }

    @Test
    @DisplayName("name이 비어 있으면 테마 생성에 실패한다.")
    void create_theme_without_name() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("description", themeDescription);
        params.put("thumbnailUrl", thumbnailUrl);
        params.put("amount", amount);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(NAME_IS_NULL.getHttpStatus().value())
                .body("message", is("요청 값 검증에 실패했습니다."));
    }

    @Test
    @DisplayName("description이 비어 있으면 테마 생성에 실패한다.")
    void create_theme_without_description() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", themeName);
        params.put("description", "");
        params.put("thumbnailUrl", thumbnailUrl);
        params.put("amount", amount);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(DESCRIPTION_IS_NULL.getHttpStatus().value())
                .body("message", is("요청 값 검증에 실패했습니다."));
    }

    @Test
    @DisplayName("thumbnailUrl이 없으면 테마 생성에 실패한다.")
    void create_theme_without_thumbnail_url() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", themeName);
        params.put("description", themeDescription);
        params.put("thumbnailUrl", null);
        params.put("amount", amount);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(THUMBNAIL_URL_IS_NULL.getHttpStatus().value())
                .body("message", is("요청 값 검증에 실패했습니다."));
    }

    @Test
    @DisplayName("thumbnailUrl이 비어있어도 기본 썸네일 테마를 생성한다.")
    void create_theme_empty_thumbnail_url() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", themeName);
        params.put("description", themeDescription);
        params.put("thumbnailUrl", "");
        params.put("amount", amount);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .body("thumbnailUrl", is(defaultThumbnailUrl))
                .statusCode(200);
    }

    @Test
    @DisplayName("amount가 없으면 테마 생성에 실패한다.")
    void create_theme_without_amount() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", themeName);
        params.put("description", themeDescription);
        params.put("thumbnailUrl", thumbnailUrl);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(AMOUNT_IS_NULL.getHttpStatus().value())
                .body("message", is("요청 값 검증에 실패했습니다."));
    }

    @Test
    @DisplayName("amount가 음수이면 테마 생성에 실패한다.")
    void create_theme_negative_amount() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", themeName);
        params.put("description", themeDescription);
        params.put("thumbnailUrl", thumbnailUrl);
        params.put("amount", -1L);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(AMOUNT_IS_NEGATIVE.getHttpStatus().value())
                .body("message", is("요청 값 검증에 실패했습니다."));
    }

}
