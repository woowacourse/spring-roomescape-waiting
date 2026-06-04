package roomescape.history;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        createReservation(1L, "아루", LocalDate.now().plusDays(1), 1L, 1L);
        createReservation(2L, "쿠다", LocalDate.now().plusDays(2), 1L, 2L);
        createReservationWaiting(1L, LocalDate.now().plusDays(1), 1L, 1L, "다른이름", "2026-05-29 10:00:00");
        createReservationWaiting(2L, LocalDate.now().plusDays(1), 1L, 1L, "아루", "2026-05-30 09:00:00");
    }

    @Test
    @DisplayName("이름으로 내 예약과 대기 내역을 함께 조회한다")
    void getHistoriesByName() throws Exception {
        mockMvc.perform(get("/historys/아루"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("RESERVATION"))
                .andExpect(jsonPath("$[0].name").value("아루"))
                .andExpect(jsonPath("$[0].date").value(LocalDate.now().plusDays(1).toString()))
                .andExpect(jsonPath("$[0].theme.name").value("미술관의 밤"))
                .andExpect(jsonPath("$[0].time.startAt").value("10:00:00"))
                .andExpect(jsonPath("$[0].sequence").value(0))
                .andExpect(jsonPath("$[1].status").value("WAITING"))
                .andExpect(jsonPath("$[1].name").value("아루"))
                .andExpect(jsonPath("$[1].date").value(LocalDate.now().plusDays(1).toString()))
                .andExpect(jsonPath("$[1].theme.name").value("미술관의 밤"))
                .andExpect(jsonPath("$[1].time.startAt").value("10:00:00"))
                .andExpect(jsonPath("$[1].sequence").value(2));
    }

    @Test
    @DisplayName("등록 날짜가 다르면 날짜 + 시간 기준으로 순번이 결정된다")
    void sequenceIsOrderedByRegistrationDateTime() throws Exception {
        // 어제 10:00 등록
        createReservationWaiting(3L, LocalDate.now().plusDays(2), 1L, 2L, "먼저등록",
                String.valueOf(LocalDateTime.now().minusDays(1).withHour(10)));
        // 오늘 09:00 등록
        createReservationWaiting(4L, LocalDate.now().plusDays(2), 1L, 2L, "나중등록",
                String.valueOf(LocalDateTime.now().withHour(9)));

        mockMvc.perform(get("/historys/먼저등록"))
                .andExpect(jsonPath("$[0].sequence").value(1));

        mockMvc.perform(get("/historys/나중등록"))
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
                "INSERT INTO reservation (id, name, date, theme_id, time_id) VALUES (?, ?, ?, ?, ?)",
                id,
                name,
                date,
                themeId,
                timeId
        );
    }

    private void createReservationWaiting(
            final long id,
            final LocalDate date,
            final long themeId,
            final long timeId,
            final String name,
            final String requestedAt
    ) {
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (id, date, theme_id, time_id, name, requested_at) VALUES (?, ?, ?, ?, ?, ?)",
                id,
                date,
                themeId,
                timeId,
                name,
                requestedAt
        );
    }
}
