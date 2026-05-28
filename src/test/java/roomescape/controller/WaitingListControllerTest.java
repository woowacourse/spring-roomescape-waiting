package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.ClearDbTest;
import roomescape.dto.WaitingListResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ClearDbTest
class WaitingListControllerTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final String STRING_TOMORROW = TOMORROW.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void 예약_대기_추가() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");
        jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "류시", STRING_TOMORROW, "1", "1", LocalDateTime.now().minusDays(1));

        Map<String, Object> params = new HashMap<>();
        params.put("name", "검프");
        params.put("date", STRING_TOMORROW);
        params.put("timeId", 1);
        params.put("themeId", 1);

        WaitingListResult response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waiting-list")
                .then().log().all()
                .statusCode(201).extract()
                .jsonPath().getObject(".", WaitingListResult.class);

        assertThat(response.id()).isEqualTo(2);
        assertThat(response.waitingOrder()).isEqualTo(2);
        assertThat(response.name()).isEqualTo("검프");
        assertThat(response.date()).isEqualTo(TOMORROW);
        assertThat(response.timeId()).isEqualTo(1);
        assertThat(response.themeId()).isEqualTo(1);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void 예약_대기_삭제() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "검프", STRING_TOMORROW, "1", "1", LocalDateTime.now().minusDays(1));

        Map<String, Object> params = new HashMap<>();
        params.put("name", "검프");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().delete("/waiting-list/1")
                .then().log().all()
                .statusCode(204);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
        assertThat(count).isZero();
    }

    @Test
    void 예약_대기_조회() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "11:00", "11:30");

        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "오즈의마법사", "판타지 테마", "http:~");

        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");
        jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "검프", STRING_TOMORROW, "1", "1", LocalDateTime.now().minusDays(1));
        jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "류시", STRING_TOMORROW, "1", "1", LocalDateTime.now().minusDays(1));
        jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "류시", STRING_TOMORROW, "2", "2", LocalDateTime.now().minusDays(1));

        List<WaitingListResult> responses = RestAssured.given().log().all()
                .when().get("/waiting-list?name=검프")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", WaitingListResult.class);

        assertThat(responses.size()).isEqualTo(1);

        WaitingListResult response = responses.getFirst();
        assertThat(response.id()).isEqualTo(1);
        assertThat(response.waitingOrder()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("검프");
        assertThat(response.date()).isEqualTo(TOMORROW);
        assertThat(response.timeId()).isEqualTo(1);
        assertThat(response.themeId()).isEqualTo(1);

        List<WaitingListResult> responses2 = RestAssured.given().log().all()
                .when().get("/waiting-list?name=류시")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", WaitingListResult.class);

        assertThat(responses2.size()).isEqualTo(2);

        WaitingListResult response2 = responses2.getFirst();
        assertThat(response2.id()).isEqualTo(2);
        assertThat(response2.waitingOrder()).isEqualTo(2);
        assertThat(response2.name()).isEqualTo("류시");
        assertThat(response2.date()).isEqualTo(TOMORROW);
        assertThat(response2.timeId()).isEqualTo(1);
        assertThat(response2.themeId()).isEqualTo(1);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
        assertThat(count).isEqualTo(3);
    }
}
