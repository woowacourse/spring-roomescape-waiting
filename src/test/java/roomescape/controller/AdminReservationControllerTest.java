package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.ClearDbTest;
import roomescape.dto.response.ReservationResult;

@ClearDbTest
class AdminReservationControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Nested
    class 전체_예약_조회 {

        @Test
        void 전체_예약_목록_반환() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-05-05", "1", "1");
            jdbcTemplate.update("INSERT INTO orders (order_id, idempotency_key, amount, reservation_id, status) VALUES (?, ?, ?, ?, ?)", "order-001", "idem-key-001", 50000, 1, "PENDING");
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "류시", "2026-05-06", "1", "1");
            jdbcTemplate.update("INSERT INTO orders (order_id, idempotency_key, amount, reservation_id, status) VALUES (?, ?, ?, ?, ?)", "order-002", "idem-key-002", 50000, 2, "PENDING");

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
        void 예약이_없으면_빈_목록_반환() {
            List<ReservationResult> reservations = RestAssured.given().log().all()
                    .when().get("/admin/reservations")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", ReservationResult.class);

            assertThat(reservations).isEmpty();
        }
    }

    @Nested
    class 관리자_예약_삭제 {

        @BeforeEach
        void setUp() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                    "브라운", LocalDate.now().plusDays(1), "1", "1");
        }

        @Test
        void 성공() {
            RestAssured.given().log().all()
                    .when().delete("/admin/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            Integer count = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
            assertThat(count).isZero();
        }

        @Test
        void 존재하지_않는_예약_삭제_시도시_404() {
            RestAssured.given().log().all()
                    .when().delete("/admin/reservations/999")
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
