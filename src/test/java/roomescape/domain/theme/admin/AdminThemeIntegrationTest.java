package roomescape.domain.theme.admin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.domain.theme.Theme;
import roomescape.support.TestFixture;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AdminThemeIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestFixture testFixture;

    @Value("${token}")
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        testFixture.clear();
    }

    @Test
    @DisplayName("관리자의 테마 전체 조회를 end-to-end로 확인한다.")
    void getAllThemeForAdmin() {
        testFixture.saveTheme("공포");

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/themes")
            .then().log().all()
            .statusCode(200)
            .body("[0].name", is("공포"))
            .body("[0].content", is("무서운 테마"))
            .body("[0].url", is("theme-url"));
    }

    @Test
    @DisplayName("관리자가 토큰을 누락했을 경우 401 예외가 발생한다.")
    void getAllThemeForAdminWithoutToken() {
        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/admin/themes")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    @DisplayName("관리자의 테마 생성을 end-to-end로 확인한다.")
    void createTheme() {
        String request = """
            {
                "name" : "공포",
                "content": "두렵다",
                "url": "theme-scare"
            }
            """;

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .body(request)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(201)
            .body("name", is("공포"))
            .body("content", is("두렵다"))
            .body("url", is("theme-scare"));

        given()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/themes")
            .then()
            .statusCode(200)
            .body("name", hasItem("공포"))
            .body("content", hasItem("두렵다"))
            .body("url", hasItem("theme-scare"));
    }

    @Test
    @DisplayName("관리자 테마 생성 시 내용 필드가 누락되었을 경우 400 에러가 발생한다.")
    void createThemeWithOutContent() {
        String request = """
            {
                "name" : "공포",
                "url": "theme-scare"
            }
            """;

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .body(request)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(400)
            .body("code", is("INPUT_VALIDATION_ERROR"))
            .body("message", is("테마 내용은 비어있을 수 없습니다."));
    }

    @Test
    @DisplayName("관리자 테마 생성 시 관리자 인증 토큰이 누락되었을 경우 401 에러가 발생한다.")
    void createThemeWithOutToken() {
        String request = """
            {
                "name" : "공포",
                "content": "두렵다",
                "url": "theme-scare"
            }
            """;

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    @DisplayName("관리자의 테마 삭제를 end-to-end로 확인한다.")
    void deleteTheme() {
        Theme theme = testFixture.saveTheme("공포");

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().delete("/admin/themes/{id}", theme.getId())
            .then().log().all()
            .statusCode(204);

        given()
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/themes")
            .then()
            .statusCode(200)
            .body("name", not(hasItem("공포")));
    }
}
