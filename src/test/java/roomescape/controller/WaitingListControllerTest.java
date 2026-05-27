package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("검프");
        assertThat(response.date()).isEqualTo(TOMORROW);
        assertThat(response.timeId()).isEqualTo(1);
        assertThat(response.themeId()).isEqualTo(1);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
        assertThat(count).isEqualTo(1);
    }
}
