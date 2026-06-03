package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;
import roomescape.fixture.Scenario;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("GET /themes - 목록을 조회한다")
    void getThemes() {
        DbFixtures.insertTheme(jdbcTemplate, "공포");

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(1));
    }

    @Test
    @DisplayName("GET /themes/{id} - 단건을 조회한다")
    void getTheme() {
        long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");

        RestAssured.given().log().all()
                .when().get("/themes/" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("name", equalTo("공포"));
    }

    @Test
    @DisplayName("GET /themes/{id} - 없는 id면 404과 메시지를 반환한다")
    void getThemeReturns404WhenIdDoesNotExist() {
        RestAssured.given().log().all()
                .when().get("/themes/9999")
                .then().log().all()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /themes/{id}/times - 예약된 시간은 isReserved true, 나머지는 false")
    void getThemeTimesMarksReservedTimes() {
        long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        long reservedTimeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        long freeTimeId = DbFixtures.insertTime(jdbcTemplate, "11:00");
        String date = Fixtures.daysFromNow(-1).toString();
        Scenario.reservation(jdbcTemplate)
                .member("브라운").onTheme(themeId).onTime(reservedTimeId).date(date).save();

        RestAssured.given().log().all()
                .when().get("/themes/" + themeId + "/times?date=" + date)
                .then().log().all()
                .statusCode(200)
                .body("times.find { it.id == " + reservedTimeId + " }.isReserved", is(true))
                .body("times.find { it.id == " + freeTimeId + " }.isReserved", is(false));
    }

    @Test
    @DisplayName("GET /themes/popular - 인기 테마 목록을 조회한다")
    void getPopularThemes() {
        long themeId = Scenario.themeWithReservations(jdbcTemplate, 1);

        RestAssured.given().log().all()
                .when().get("/themes/popular?limit=10")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(1))
                .body("themes[0].id", is((int) themeId))
                .body("themes[0].reservedCount", is(1));
    }
}
