package roomescape.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class ReservationWaitingMysqlIndexTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withInitScript("schema.sql");

    private JdbcTemplate jdbcTemplate;
    private Long targetReservationId;

    @BeforeEach
    void setUp() {
        DataSource dataSource = DataSourceBuilder.create()
                .url(MYSQL.getJdbcUrl())
                .username(MYSQL.getUsername())
                .password(MYSQL.getPassword())
                .driverClassName(MYSQL.getDriverClassName())
                .build();
        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("DELETE FROM reservation_time");

        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00:00')");
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

        jdbcTemplate.execute("ANALYZE TABLE reservation_waiting");
    }

    @Test
    void MySQL_findEarliest는_복합_인덱스를_타고_정렬을_회피한다() {
        Map<String, Object> plan = explainRow(
                "EXPLAIN SELECT * FROM reservation_waiting WHERE reservation_id = ? ORDER BY created_at, id LIMIT 1",
                targetReservationId
        );

        System.out.println("=== MySQL PLAN ===");
        System.out.println(plan);

        assertThat(plan.get("key")).isEqualTo("idx_waiting_reservation_created");
        assertThat(String.valueOf(plan.get("Extra"))).doesNotContainIgnoringCase("filesort");
    }

    private Map<String, Object> explainRow(String sql, Object... args) {
        return jdbcTemplate.queryForList(sql, args).get(0);
    }
}
