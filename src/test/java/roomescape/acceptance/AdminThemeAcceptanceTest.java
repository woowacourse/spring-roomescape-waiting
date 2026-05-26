package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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
    void POST_admin_themes_테마를_생성한다() {
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
    void POST_admin_themes_본문의_name이_누락되면_400과_메시지를_반환한다() {
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
                .body("message", equalTo("name은(는) 필수 입력값입니다."));
    }

    @Test
    void DELETE_admin_themes_id_테마를_삭제한다() {
        long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/themes/" + themeId)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void DELETE_admin_themes_없는_id면_404과_메시지를_반환한다() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/themes/9999")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("테마을(를) 찾을 수 없습니다. id=9999"));
    }
}