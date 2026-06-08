package roomescape.theme.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.controller.dto.response.ThemeResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ThemeControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("테마 추가 및 삭제")
    void createAndDeleteTheme() {

        Map<String, Object> params = new HashMap<>();
        params.put("name", "링");
        params.put("description", "공포 테마");
        params.put("thumbnailUrl", "https://~");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from theme", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from theme", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("테마 설명이 비어있으면 400을 응답한다")
    void respondBadRequestWhenThemeDescriptionIsBlank() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "링",
                        "description", "",
                        "thumbnailUrl", "https://~"
                ))
                .when().post("/themes")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("테마 설명을 입력해야 합니다."));
    }

    @Test
    @Sql(scripts = {
            "/clear.sql",
            "/popular-themes-test-data.sql"
    })
    @DisplayName("최근 일주일간 예약이 많은 상위 10개 테마 조회")
    void findTopTenPopularThemesByReservationsInLastWeek() {
        List<ThemeResponse> popularThemes = RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ThemeResponse.class);

        assertThat(popularThemes.size()).isEqualTo(10);
        assertThat(popularThemes).doesNotContain(
                new ThemeResponse(11L, "마녀의 숲", "깊은 숲속 마녀의 오두막에서 숨겨진 계약서를 찾는 판타지 테마", "https://example.com/images/witch-forest.jpg"),
                new ThemeResponse(12L, "사라진 열차", "한밤중 흔적 없이 사라진 열차의 비밀을 추적하는 추리 테마", "https://example.com/images/missing-train.jpg")
        );
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("존재하지 않는 테마를 삭제하면 404를 응답한다")
    void respondNotFoundWhenDeletingNonExistingTheme() {
        RestAssured.given().log().all()
                .when().delete("/themes/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("해당 테마에 예약이 있으면 테마 삭제시 409를 응답한다")
    void respondConflictWhenDeletingThemeInUse() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation_slot (reservation_date, time_id, theme_id) VALUES (?, ?, ?)", "2026-08-05", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation (customer_name, customer_email, slot_id) VALUES (?, ?, ?)", "브라운", "brown@example.com", "1");

        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(409)
                .body("message", org.hamcrest.Matchers.is("해당 테마에 예약이 존재하여 삭제할 수 없습니다."));
    }
}
