package roomescape.domain.theme.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.fixture.LocalDateFixture.AFTER_TWO_DAYS_DATE;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.ControllerTest;
import roomescape.domain.theme.dto.ThemeAddRequest;

class ThemeControllerTest extends ControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "insert into member (name, email, password, role) values ('어드민', 'admin@gmail.com', '123456', 'ADMIN')");
        jdbcTemplate.update("insert into reservation_time (start_at) values('10:00')");
        jdbcTemplate.update("insert into theme (name,description,thumbnail) values('리비', '리비 설명', 'url')");
    }

    @AfterEach
    void setDown() {
        jdbcTemplate.update("delete from reservation");
    }

    @DisplayName("전체 테마를 조회할 수 있다 (200 OK)")
    @Test
    void should_get_theme_list() {
        jdbcTemplate.update("insert into reservation (date, time_id, theme_id, member_id) values(?,?,?,?)"
                , AFTER_TWO_DAYS_DATE, 1, 1, 1);
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("테마를 추가할 수 있다 (201 created)")
    @Test
    void should_add_theme() {
        ThemeAddRequest themeAddRequest = new ThemeAddRequest("도도", "배고픔", "url");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeAddRequest)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("테마를 삭제할 수 있다 (204 no content)")
    @Test
    void should_remove_theme() {
        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("인기 테마 목록을 불러올 수 있다.(200 OK)")
    @Test
    void should_response_theme_ranking() {
        RestAssured.given().log().all()
                .when().get("/theme-ranking")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }
}
