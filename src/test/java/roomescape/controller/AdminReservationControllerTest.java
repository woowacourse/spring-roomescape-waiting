package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.ClearDbTest;
import roomescape.service.dto.result.ReservationResult;

@ClearDbTest
class AdminReservationControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void 전체_예약목록_조회() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-05-05", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "류시", "2026-05-06", "1", "1");

        List<ReservationResult> reservations = RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResult.class);

        assertThat(reservations).hasSize(2);
        ReservationResult reservation1 = reservations.getFirst();
        assertThat(reservation1.id()).isEqualTo(1);
        assertThat(reservation1.name()).isEqualTo("브라운");
        assertThat(reservation1.date()).isEqualTo(LocalDate.of(2026, 5, 5));
        assertThat(reservation1.time().id()).isEqualTo(1);
        assertThat(reservation1.theme().id()).isEqualTo(1);

        ReservationResult reservation2 = reservations.get(1);
        assertThat(reservation2.id()).isEqualTo(2);
        assertThat(reservation2.name()).isEqualTo("류시");
        assertThat(reservation2.date()).isEqualTo(LocalDate.of(2026, 5, 6));
        assertThat(reservation2.time().id()).isEqualTo(1);
        assertThat(reservation2.theme().id()).isEqualTo(1);
    }

    @Test
    void 예약_삭제() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-05-05", "1", "1");

        RestAssured.given().log().all()
                .when().delete("admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
        assertThat(count).isZero();
    }
}
