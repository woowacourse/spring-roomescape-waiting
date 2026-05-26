package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.service.dto.response.ReservationResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminReservationControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(
                    LocalDate.of(2026, 5, 1)
                            .atStartOfDay(ZoneId.of("Asia/Seoul"))
                            .toInstant(),
                    ZoneId.of("Asia/Seoul")
            );
        }
    }

    @Test
    @Sql("/clear.sql")
    void 예약_목록이_없으면_빈_배열을_응답한다() {
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @Sql("/clear.sql")
    void 예약_목록을_조회한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-08-05", "1", "1");

        List<ReservationResponse> reservations = RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(reservations).hasSize(count);
    }

    @Test
    @Sql("/clear.sql")
    void 생성한_예약을_관리자_목록에서_조회한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "date", "2026-08-05",
                        "timeId", 1,
                        "themeId", 1
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @Sql("/clear.sql")
    void 관리자는_예약일_당일에도_예약_일정을_수정할_수_있다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-05-01", "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-05-02",
                        "timeId", 2
                ))
                .when().put("/admin/reservations/1")
                .then().log().all()
                .statusCode(200)
                .body("id", is(1))
                .body("date", is("2026-05-02"))
                .body("time.id", is(2));

        Map<String, Object> updatedReservation = jdbcTemplate.queryForMap(
                "SELECT date, time_id FROM reservation WHERE id = ?",
                1L
        );
        assertThat(updatedReservation.get("DATE").toString()).isEqualTo("2026-05-02");
        assertThat(updatedReservation.get("TIME_ID")).isEqualTo(2L);
    }

    @Test
    @Sql("/clear.sql")
    void 관리자는_예약을_삭제할_수_있다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-08-05", "1", "1");

        RestAssured.given().log().all()
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isZero();
    }

    @Test
    @Sql("/clear.sql")
    void 존재하지_않는_예약을_관리자가_삭제하면_404를_응답한다() {
        RestAssured.given().log().all()
                .when().delete("/admin/reservations/999")
                .then().log().all()
                .statusCode(404)
                .body("message", is("존재하지 않는 예약입니다."));
    }
}
