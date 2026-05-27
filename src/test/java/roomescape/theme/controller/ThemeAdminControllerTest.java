package roomescape.theme.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.theme.exception.ThemeErrorInformation.DESCRIPTION_IS_NULL;
import static roomescape.theme.exception.ThemeErrorInformation.NAME_IS_NULL;
import static roomescape.theme.exception.ThemeErrorInformation.THUMBNAIL_URL_IS_NULL;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import roomescape.common.AcceptanceTest;

class ThemeAdminControllerTest extends AcceptanceTest {

    private final String themeName = "테마1";
    private final String themeDescription = "테마1 설명";
    private final String thumbnailUrl = "테마1 썸네일";
    private final String defaultThumbnailUrl = "DEFAULT_THUMBNAIL_URL";


    @Nested
    @DisplayName("getThemes 메서드는")
    class GetThemesTest {


        @Test
        @DisplayName("테마를 조회한다")
        void 성공() {
            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
        }
    }

    @Nested
    @DisplayName("createTheme 메서드는")
    class CreateThemeTest {


        @Test
        @DisplayName("테마를 생성한다")
        void 성공1() {
            createTheme(managerToken, themeName);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
        }


        @Test
        @DisplayName("썸네일 url이 비어있어도 생성이 가능하다")
        void 성공2() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", themeName);
            params.put("description", themeDescription);
            params.put("thumbnailUrl", "");

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
        @DisplayName("이름 없이 생성하려고 하면 400을 반환한다")
        void 실패1() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "");
            params.put("description", themeDescription);
            params.put("thumbnailUrl", thumbnailUrl);

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
        @DisplayName("설명 없이 생성하려고 하면 400을 반환한다")
        void 실패2() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", themeName);
            params.put("description", "");
            params.put("thumbnailUrl", thumbnailUrl);

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
        @DisplayName("썸네일 Url 없이 생성하려고 하면 400을 반환한다")
        void 실패3() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", themeName);
            params.put("description", themeDescription);
            params.put("thumbnailUrl", null);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(THUMBNAIL_URL_IS_NULL.getHttpStatus().value())
                .body("message", is("요청 값 검증에 실패했습니다."));
        }
    }

    @Nested
    @DisplayName("updateThemeStatus 메서드는")
    class UpdateThemeStatusTest {


        @Test
        @DisplayName("테마 활성 상태를 변경한다")
        void 성공() {
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
    }
}
