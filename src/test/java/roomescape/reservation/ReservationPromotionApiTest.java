package roomescape.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:roomescape-mockmvc")
@AutoConfigureMockMvc
class ReservationPromotionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        clearTables();
        createTheme(1L);
        createReservationTime(1L, "10:00:00");
        // 슬롯(예약 #1)의 확정 예약자는 쿠다, 대기열은 신청 시각 순서대로 아루 → 브라운 → 카리나
        createReservation(1L, "쿠다", 1L, 1L);
        createReservationWaiting(1L, 1L, "아루", "2026-08-06 12:00:00");
        createReservationWaiting(2L, 1L, "브라운", "2026-08-06 12:01:00");
        createReservationWaiting(3L, 1L, "카리나", "2026-08-06 12:02:00");
    }

    @Test
    @DisplayName("예약을 취소하면 1순위 대기(아루)가 예약으로 전환된다")
    void promoteEarliestWaitingOnCancel() throws Exception {
        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isNoContent());

        String owner = jdbcTemplate.queryForObject(
                "SELECT name FROM reservation WHERE id = 1", String.class);
        Integer aruWaitingCount = jdbcTemplate.queryForObject(
                "SELECT count(1) FROM reservation_waiting WHERE reservation_id = 1 AND name = '아루'",
                Integer.class);

        Assertions.assertThat(owner).isEqualTo("아루");
        Assertions.assertThat(aruWaitingCount).isNotNull().isZero();
    }

    @Test
    @DisplayName("전환 후 남은 대기들의 순번이 재정렬된다")
    void reorderRemainingWaitingsAfterPromotion() throws Exception {
        // 전환 전: 아루 1, 브라운 2, 카리나 3
        mockMvc.perform(get("/my-histories/브라운"))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].sequence").value(2));

        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isNoContent());

        // 전환 후: 브라운 1, 카리나 2 (아루는 예약으로 승격되어 대기열에서 빠짐)
        mockMvc.perform(get("/my-histories/브라운"))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].sequence").value(1));
        mockMvc.perform(get("/my-histories/카리나"))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].sequence").value(2));
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    private void createTheme(final long id) {
        jdbcTemplate.update(
                "INSERT INTO theme (id, name, description, thumbnail_url) VALUES (?, ?, ?, ?)",
                id, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
    }

    private void createReservationTime(final long id, final String startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (?, ?)", id, startAt);
    }

    private void createReservation(final long id, final String name, final long themeId, final long timeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (id, name, date, theme_id, time_id) VALUES (?, ?, ?, ?, ?)",
                id, name, "2026-08-06", themeId, timeId);
    }

    private void createReservationWaiting(
            final long id,
            final long reservationId,
            final String name,
            final String requestedAt
    ) {
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (id, reservation_id, name, requested_at) VALUES (?, ?, ?, ?)",
                id, reservationId, name, requestedAt);
    }
}
