package roomescape.controller.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.controller.BaseControllerTest;

public class ThemeApiControllerTest extends BaseControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES ('방탈출1', '1번 방탈출', '썸네일1')");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES ('방탈출2', '2번 방탈출', '썸네일2')");

    }

    @Test
    @DisplayName("테마 조회를 정상적으로 수행한다.")
    void findTheme_Success() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(2));
    }

    @Test
    @DisplayName("테마 추가를 정상적으로 수행한다.")
    void addTheme_Success() {
        Map<String, String> params = Map.of("name", "레벨2 탈출",
                "description", "우테코 레벨2를 탈출하는 내용입니다.",
                "thumbnail", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .body("id", is(3));

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(3));
    }

    @Test
    @DisplayName("테마 삭제를 정상적으로 수행한다.")
    void deleteTheme_Success() {
        RestAssured.given().log().all()
                .when().delete("/themes/2")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(1));
    }
}
