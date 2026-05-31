package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

    @Nested
    class 예약_대기_조회 {

        @BeforeEach
        void setUp() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "11:00", "11:30");
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "오즈의마법사", "판타지 테마", "http:~");
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");
            jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "검프", STRING_TOMORROW, "1", "1", LocalDateTime.now().minusDays(1));
            jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "류시", STRING_TOMORROW, "1", "1", LocalDateTime.now().minusDays(1));
            jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "류시", STRING_TOMORROW, "2", "2", LocalDateTime.now().minusDays(1));
        }

        @Test
        void 대기가_1건인_사용자는_1건이_조회() {
            List<WaitingListResult> responses = RestAssured.given().log().all()
                    .when().get("/waiting-list?name=검프")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", WaitingListResult.class);

            assertThat(responses).hasSize(1);

            WaitingListResult response = responses.getFirst();
            assertThat(response.id()).isEqualTo(1);
            assertThat(response.waitingOrder()).isEqualTo(1);
            assertThat(response.name()).isEqualTo("검프");
            assertThat(response.date()).isEqualTo(TOMORROW);
            assertThat(response.timeId()).isEqualTo(1);
            assertThat(response.themeId()).isEqualTo(1);
        }

        @Test
        void 대기가_2건인_사용자는_2건이_조회() {
            List<WaitingListResult> responses = RestAssured.given().log().all()
                    .when().get("/waiting-list?name=류시")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", WaitingListResult.class);

            assertThat(responses).hasSize(2);

            WaitingListResult first = responses.get(0);
            assertThat(first.id()).isEqualTo(2);
            assertThat(first.waitingOrder()).isEqualTo(2);
            assertThat(first.timeId()).isEqualTo(1);
            assertThat(first.themeId()).isEqualTo(1);

            WaitingListResult second = responses.get(1);
            assertThat(second.id()).isEqualTo(3);
            assertThat(second.waitingOrder()).isEqualTo(1);
            assertThat(second.timeId()).isEqualTo(2);
            assertThat(second.themeId()).isEqualTo(2);
        }

        @Test
        void 없는_이름이면_빈_목록이_조회() {
            List<WaitingListResult> responses = RestAssured.given().log().all()
                    .when().get("/waiting-list?name=없는사람")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", WaitingListResult.class);

            assertThat(responses).isEmpty();
        }
    }
}
