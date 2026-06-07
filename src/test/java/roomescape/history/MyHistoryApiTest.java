package roomescape.history;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:my-history-api")
@AutoConfigureMockMvc
class MyHistoryApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        clearTables();
        createTheme(1L, "미술관의 밤");
        createReservationTime(1L, "10:00:00");
        createReservationTime(2L, "11:00:00");
        createReservation(1L, "아루", LocalDate.parse("2026-08-06"), 1L, 1L);
        createReservation(2L, "쿠다", LocalDate.parse("2026-08-07"), 1L, 2L);
        createReservationWaiting(1L, 2L, "다른이름", "2026-08-05 11:59:00");
        createReservationWaiting(2L, 2L, "아루", "2026-08-05 12:00:00");
    }

    @Test
    @DisplayName("이름으로 내 예약과 대기 내역을 함께 조회한다")
    void getHistoriesByName() throws Exception {
        mockMvc.perform(get("/historys/아루"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("RESERVATION"))
                .andExpect(jsonPath("$[0].name").value("아루"))
                .andExpect(jsonPath("$[0].date").value("2026-08-06"))
                .andExpect(jsonPath("$[0].theme.name").value("미술관의 밤"))
                .andExpect(jsonPath("$[0].time.startAt").value("10:00:00"))
                .andExpect(jsonPath("$[0].sequence").value(0))
                .andExpect(jsonPath("$[1].status").value("WAITING"))
                .andExpect(jsonPath("$[1].name").value("아루"))
                .andExpect(jsonPath("$[1].date").value("2026-08-07"))
                .andExpect(jsonPath("$[1].theme.name").value("미술관의 밤"))
                .andExpect(jsonPath("$[1].time.startAt").value("11:00:00"))
                .andExpect(jsonPath("$[1].sequence").value(2));
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_slot");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    private void createTheme(final long id, final String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (id, name, description, thumbnail_url) VALUES (?, ?, ?, ?)",
                id,
                name,
                "추리 테마",
                "https://example.com/theme.png"
        );
    }

    private void createReservationTime(final long id, final String startAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                id,
                startAt
        );
    }

    private void createReservation(
            final long id,
            final String name,
            final LocalDate date,
            final long themeId,
            final long timeId
    ) {
        jdbcTemplate.update(
                "INSERT INTO reservation_slot (id, date, theme_id, time_id) VALUES (?, ?, ?, ?)",
                id,
                date,
                themeId,
                timeId
        );
        jdbcTemplate.update(
                "INSERT INTO reservation (id, name, slot_id) VALUES (?, ?, ?)",
                id,
                name,
                id
        );
    }

    private void createReservationWaiting(
            final long id,
            final long slotId,
            final String name,
            final String requestedAt
    ) {
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (id, slot_id, name, requested_at) VALUES (?, ?, ?, ?)",
                id,
                slotId,
                name,
                requestedAt
        );
    }
}
