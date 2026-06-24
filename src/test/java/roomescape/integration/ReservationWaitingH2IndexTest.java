package roomescape.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@TestPropertySource(properties = "spring.sql.init.data-locations=")
class ReservationWaitingH2IndexTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long targetReservationId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "공포", "무서운 테마", "https://example.com/horror.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);

        for (int r = 0; r < 5; r++) {
            jdbcTemplate.update(
                    "INSERT INTO reservation (name, date, time_id, theme_id, reservation_status) VALUES (?, ?, ?, ?, 'CONFIRM')",
                    "예약자" + r, LocalDate.of(2026, 8, 5), timeId, themeId
            );
        }
        List<Long> reservationIds = jdbcTemplate.queryForList("SELECT id FROM reservation", Long.class);

        int counter = 0;
        for (Long reservationId : reservationIds) {
            LocalDateTime base = LocalDateTime.of(2026, 8, 1, 10, 0, 0);
            for (int w = 0; w < 100; w++) {
                jdbcTemplate.update(
                        "INSERT INTO reservation_waiting (name, created_at, reservation_id) VALUES (?, ?, ?)",
                        "user" + counter++, base.plusSeconds(w), reservationId
                );
            }
        }
        targetReservationId = reservationIds.get(0);
    }

    @Test
    void H2_findEarliest_실행계획을_확인한다() {
        String plan = explainPlan(
                "SELECT * FROM reservation_waiting WHERE reservation_id = ? ORDER BY created_at, id LIMIT 1",
                targetReservationId
        );

        System.out.println("=== H2 PLAN ===");
        System.out.println(plan);

        assertThat(plan).doesNotContainIgnoringCase("idx_waiting_reservation_created");
    }

    private String explainPlan(String sql, Object... args) {
        List<String> rows = jdbcTemplate.queryForList("EXPLAIN " + sql, String.class, args);
        return String.join(System.lineSeparator(), rows);
    }
}
