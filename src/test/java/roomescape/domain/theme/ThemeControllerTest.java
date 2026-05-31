package roomescape.domain.theme;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")
class ThemeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("사용자 권한으로 모든 테마를 조회한다.")
    void getAllThemes() {
        themeRepository.save(Theme.createWithoutId("테스트테마", "설명", "url"));

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
