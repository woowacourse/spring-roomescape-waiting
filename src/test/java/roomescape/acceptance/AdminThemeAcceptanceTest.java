package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.DbFixtures;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminThemeAcceptanceTest {

    private static final String AUTHORIZATION = "Authorization";
    private static final String MANAGER_NAME = "관리자";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private String managerBearer() {
        return DbFixtures.managerBearer(jdbcTemplate, MANAGER_NAME);
    }

    @Test
    @DisplayName("POST /admin/themes - 테마를 생성한다")
    void createTheme() {
        Map<String, Object> body = Map.of(
                "name", "공포",
                "description", "무서움",
                "thumbnailImageUrl", "https://thumbnail.url");

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .header("Location", matchesPattern("/themes/\\d+"));
    }

    @Test
    @DisplayName("POST /admin/themes - 본문의 name이 누락되면 400과 메시지를 반환한다")
    void createThemeReturns400WhenNameIsMissing() {
        Map<String, Object> body = Map.of(
                "description", "무서움",
                "thumbnailImageUrl", "https://thumbnail.url");

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("DELETE /admin/themes/{id} - 테마를 삭제한다")
    void deleteTheme() {
        long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/themes/" + themeId)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("DELETE /admin/themes - 없는 id면 404과 메시지를 반환한다")
    void deleteThemeReturns404WhenIdDoesNotExist() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/themes/9999")
                .then().log().all()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
    }
}
