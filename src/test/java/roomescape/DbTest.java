package roomescape;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DbTest {

    public static final String FUTURE_FIRST_DATE = LocalDate.now().plusDays(1).toString();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 데이터베이스_연동() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isNotBlank();
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION_TIME", null).next()).isTrue();
            assertThat(connection.getMetaData().getTables(null, null, "THEME", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void DB_조회_API_전환() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "15:40");
        Long timeId = jdbcTemplate.queryForObject("SELECT id from reservation_time limit 1", Long.class);
        jdbcTemplate.update(
            "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
            "공포", "무서운 테마", "https://example.com/horror.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT id from theme limit 1", Long.class);
        jdbcTemplate.update("INSERT INTO slot (date, time_id, theme_id) VALUES (?, ?, ?)", FUTURE_FIRST_DATE, timeId,
            themeId);
        Long slotId = jdbcTemplate.queryForObject("SELECT id from slot limit 1", Long.class);
        jdbcTemplate.update("INSERT INTO member (name) VALUES (?)", "브라운");
        Long memberId = jdbcTemplate.queryForObject("SELECT id from member limit 1", Long.class);
        jdbcTemplate.update("INSERT INTO reservation (member_id, slot_id) VALUES (?, ?)", memberId, slotId);

        List<Map> reservations = RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200).extract()
            .jsonPath().getList(".", Map.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }

    @Test
    void DB_추가_삭제_API_전환() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        Long timeId = jdbcTemplate.queryForObject("SELECT id from reservation_time limit 1", Long.class);
        jdbcTemplate.update(
            "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
            "공포", "무서운 테마", "https://example.com/horror.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT id from theme limit 1", Long.class);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", FUTURE_FIRST_DATE);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
            .when().delete("/reservations/1")
            .then().log().all()
            .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }
}
