package roomescape.admin.theme;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminThemeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 테마_정상_생성_확인_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마1");
        params.put("description", "테마 설명");
        params.put("imageUrl", "https://example.com/image.jpg");
        params.put("price", 50000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("테마1"))
                .body("description", is("테마 설명"))
                .body("imageUrl", is("https://example.com/image.jpg"));
    }

    @Test
    void createTheme_이름이_비어있는경우_에러_반환_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("description", "테마 설명");
        params.put("imageUrl", "https://example.com/image.jpg");
        params.put("price", 50000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("message", is("등록할 테마의 이름은 필수 입력 값입니다."));
    }

    @Test
    void createTheme_중복된_이름인경우_에러_반환_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마1");
        params.put("description", "테마 설명");
        params.put("imageUrl", "https://example.com/image.jpg");
        params.put("price", 50000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(409)
                .body("message", is("이미 등록된 테마 이름입니다."));
    }

    @Test
    void 테마_전체_조회_정상_동작_테스트() {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES (?, ?, ?)",
                "테마1", "설명1", "https://example.com/1.jpg"
        );
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES (?, ?, ?)",
                "테마2", "설명2", "https://example.com/2.jpg"
        );

        RestAssured.given().log().all()
                .when().get("/admin/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(2))
                .body("themes[0].name", is("테마1"))
                .body("themes[1].name", is("테마2"));
    }

    @Test
    void 특정_테마_삭제_정상_동작_테스트() {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES (?, ?, ?)",
                "테마1", "설명", "https://example.com/image.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?", Long.class, "테마1"
        );

        RestAssured.given().log().all()
                .when().delete("/admin/themes/" + themeId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void deleteTheme_존재하지_않는_id인경우_에러_반환_테스트() {
        RestAssured.given().log().all()
                .when().delete("/admin/themes/999")
                .then().log().all()
                .statusCode(404)
                .body("message", is("해당 theme id를 찾을 수 없습니다."));
    }

    @Test
    void deleteTheme_예약이_존재하는경우_에러_반환_테스트() {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES (?, ?, ?)",
                "테마1", "설명", "https://example.com/image.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?", Long.class, "테마1"
        );
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at, finish_at) VALUES (?, ?)",
                "10:00", "11:00"
        );
        Long timeId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?", Long.class, "10:00"
        );
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "유저", "2099-01-01", timeId, themeId
        );

        RestAssured.given().log().all()
                .when().delete("/admin/themes/" + themeId)
                .then().log().all()
                .statusCode(409)
                .body("message", is("예약이 존재하는 시간을 삭제할 수 없습니다."));
    }
}
