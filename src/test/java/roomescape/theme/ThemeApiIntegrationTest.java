package roomescape.theme;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.support.ApiIntegrationTestHelper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ThemeApiIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ApiIntegrationTestHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new ApiIntegrationTestHelper(jdbcTemplate);
        testHelper.clearDatabase();
    }

    @DisplayName("테마 생성 API를 테스트합니다.")
    @Test
    void create_theme() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "theme name");
        params.put("description", "theme description");
        params.put("thumbnailImgUrl", "theme img url");
        params.put("price", 30000L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("테마 삭제 API를 테스트합니다.")
    @Test
    void delete_theme() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "theme name");
        params.put("description", "theme description");
        params.put("thumbnailImgUrl", "theme img url");
        params.put("price", 30000L);

        Integer themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        RestAssured.given().log().all()
                .when().delete("/admin/themes/{id}", themeId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 테마 삭제 시 404를 테스트합니다.")
    @Test
    void delete_theme_not_found() {
        RestAssured.given().log().all()
                .when().delete("/admin/themes/100")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("존재하는 모든 테마 조회를 테스트합니다.")
    @Test
    void find_all_themes() {
        testHelper.insertTheme("theme name 1", "theme description 1", "theme img url 1");
        testHelper.insertTheme("theme name 2", "theme description 2", "theme img url 2");

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @DisplayName("최근 일주일 간 인기 테마 조회 API를 테스트합니다.")
    @Test
    void find_popular_themes() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long secondTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long theme1Id = testHelper.insertTheme("theme name 1", "theme description 1", "theme img url 1");
        Long theme2Id = testHelper.insertTheme("theme name 2", "theme description 2", "theme img url 2");
        LocalDate yesterday = LocalDate.now().minusDays(1);

        testHelper.insertReservation("스타크", yesterday, theme1Id, timeId);
        testHelper.insertReservation("카야", yesterday, theme2Id, timeId);
        testHelper.insertReservation("스타크", yesterday, theme1Id, secondTimeId);

        RestAssured.given().log().all()
                .queryParam("startAt", yesterday.minusWeeks(1).toString())
                .queryParam("endAt", yesterday.toString())
                .queryParam("limit", 10)
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].id", equalTo(theme1Id.intValue()))
                .body("[0].name", equalTo("theme name 1"))
                .body("[1].id", equalTo(theme2Id.intValue()))
                .body("[1].name", equalTo("theme name 2"));
    }
}
