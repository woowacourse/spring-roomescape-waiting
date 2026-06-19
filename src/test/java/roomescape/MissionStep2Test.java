package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.controller.dto.response.ReservationResponses;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStep2Test {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        jdbcTemplate.update("insert into reservation_time(start_at) values ('10:00')");
        jdbcTemplate.update(
                "insert into theme(name, description, thumbnail_url) values ('공포', '무서워요', 'https://zeze.com')");
        jdbcTemplate.update("insert into member(name) values ('브라운')");  // id=1
    }

    @Test
    void 데이터베이스_연동() throws RuntimeException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void DB_조회_API_전환() {
        jdbcTemplate.update("INSERT INTO slot (date, time_id, theme_id) VALUES (?, ?, ?)", "2023-08-05", 1, 1);
        jdbcTemplate.update("INSERT INTO reservation (slot_id, member_id, status) VALUES (?, ?, ?)", 1, 1, "APPROVED");

        ReservationResponses reservations = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .as(ReservationResponses.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        assertSoftly(softly -> {
            softly.assertThat(reservations.getReservations()).isNotNull();
            softly.assertThat(reservations.getReservations().size()).isEqualTo(count);
        });
    }

    @Test
    void DB_추가_삭제_API_전환() {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", 1L);
        params.put("date", "2099-08-05");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .param("memberId", 1)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }
}
