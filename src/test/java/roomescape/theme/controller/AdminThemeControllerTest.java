package roomescape.theme.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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
import org.springframework.test.context.jdbc.Sql;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminThemeControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url, price) VALUES ('테마A', '설명A', 'https://a.com', 10000)");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url, price) VALUES ('테마B', '설명B', 'https://b.com', 20000)");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url, price) VALUES ('테마C', '설명C', 'https://c.com', 30000)");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url, price) VALUES ('테마D', '설명D', 'https://d.com', 40000)");
    }

    private Map<String, Object> themeBody() {
        return Map.of("name", "테마5", "description", "설명", "imageUrl", "https://image.com", "price", 15000);
    }

    @Test
    @DisplayName("테마 생성 성공")
    void 테마_생성_성공() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeBody())
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo("테마5"))
                .body("price", equalTo(15000));
    }

    @Test
    @DisplayName("테마 전체 조회 성공")
    void 테마_전체_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/admin/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4));
    }

    @Test
    @DisplayName("테마 삭제 성공")
    void 테마_삭제_성공() {
        Integer id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeBody())
                .when().post("/admin/themes")
                .then().extract().path("id");

        RestAssured.given().log().all()
                .when().delete("/admin/themes/" + id)
                .then().log().all()
                .statusCode(204);
    }
}