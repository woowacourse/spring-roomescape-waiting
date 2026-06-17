package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.dto.result.ThemeResult;
import roomescape.support.SpringBootApiTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootApiTest
class ThemeControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void 전체_테마_조회() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "해리포터", "판타지 테마", "http:~");

        List<ThemeResult> themes = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ThemeResult.class);

        assertThat(themes).hasSize(2);

        ThemeResult theme1 = themes.getFirst();
        assertThat(theme1.id()).isEqualTo(1);
        assertThat(theme1.name()).isEqualTo("링");
        assertThat(theme1.description()).isEqualTo("공포 테마");
        assertThat(theme1.thumbnailUrl()).isEqualTo("http:~");

        ThemeResult theme2 = themes.get(1);
        assertThat(theme2.id()).isEqualTo(2);
        assertThat(theme2.name()).isEqualTo("해리포터");
        assertThat(theme2.description()).isEqualTo("판타지 테마");
        assertThat(theme2.thumbnailUrl()).isEqualTo("http:~");
    }

    @Test
    @Sql(scripts = {
            "/clear.sql",
            "/popular-themes-test-data.sql"
    })
    void 최근_일주일간_예약이_많은_상위_10개_테마_조회() {
        List<ThemeResult> popularThemes = RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ThemeResult.class);

        assertThat(popularThemes)
                .hasSize(10)
                .doesNotContain(
                new ThemeResult(11L, "마녀의 숲", "깊은 숲속 마녀의 오두막에서 숨겨진 계약서를 찾는 판타지 테마", "https://example.com/images/witch-forest.jpg"),
                new ThemeResult(12L, "사라진 열차", "한밤중 흔적 없이 사라진 열차의 비밀을 추적하는 추리 테마", "https://example.com/images/missing-train.jpg")
        );
    }

    @Test
    void 테마_추가() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "링");
        params.put("description", "공포 테마");
        params.put("thumbnailUrl", "https://~");

        ThemeResult response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201).extract()
                .jsonPath().getObject(".", ThemeResult.class);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("링");
        assertThat(response.description()).isEqualTo("공포 테마");
        assertThat(response.thumbnailUrl()).isEqualTo("https://~");

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from theme", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void 필수값이_누락되면_테마_추가에_실패한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("description", "공포 테마");
        params.put("thumbnailUrl", "https://~");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 테마_삭제() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");

        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(*) from theme", Integer.class);
        assertThat(countAfterDelete).isZero();
    }

    @Test
    void 존재하지_않는_테마를_삭제하면_실패한다() {
        RestAssured.given().log().all()
                .when().delete("/themes/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약이_존재하는_테마를_삭제하면_실패한다() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2024-01-01", "1", "1");

        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(409);
    }
}
