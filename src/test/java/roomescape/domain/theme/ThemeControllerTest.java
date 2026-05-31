package roomescape.domain.theme;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql("/truncate.sql")
class ThemeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("사용자 권한으로 모든 테마를 조회한다.")
    void getAllThemes() {
        jdbcTemplate.update("insert into theme(name, content, url) values (?, ?, ?)", "테스트테마", "설명", "url");

        RestAssured.given().log().all()
            .when().get("/themes")
            .then().log().all()
            .statusCode(200)
            .body("any { it.name == '테스트테마' }", is(true));
    }

    @Test
    @DisplayName("사용자 권한으로 테마 순위를 조회한다.")
    void getThemeRank() {
        RestAssured.given().log().all()
            .when().get("/themes/rank")
            .then().log().all()
            .statusCode(200);
    }
}
