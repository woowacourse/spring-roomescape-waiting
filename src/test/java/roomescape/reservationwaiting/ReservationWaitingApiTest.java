package roomescape.reservationwaiting;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:reservation-waiting-api")
@AutoConfigureMockMvc
class ReservationWaitingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        clearTables();
        createTheme(1L);
        createReservationTime(1L);
        createReservation(1L, "쿠다", LocalDate.parse("2026-08-06"), 1L, 1L);
    }

    @Test
    @DisplayName("예약 대기를 생성한다")
    void createReservationWaiting() throws Exception {
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "아루",
                                  "date": "2026-08-06",
                                  "themeId": 1,
                                  "timeId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/waitings/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("아루"))
                .andExpect(jsonPath("$.requestAt", notNullValue()))
                .andExpect(jsonPath("$.reservationResponse.id").value(1))
                .andExpect(jsonPath("$.reservationResponse.name").value("쿠다"))
                .andExpect(jsonPath("$.reservationResponse.theme.id").value(1))
                .andExpect(jsonPath("$.reservationResponse.time.id").value(1));

        Integer waitingCount = jdbcTemplate.queryForObject(
                "SELECT count(1) FROM reservation_waiting WHERE reservation_id = 1 AND name = '아루'",
                Integer.class
        );
        assert waitingCount != null;
        assertThat(waitingCount).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 예약에는 대기를 생성할 수 없다")
    void createReservationWaitingWithoutReservation() throws Exception {
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "아루",
                                  "date": "2026-08-07",
                                  "themeId": 1,
                                  "timeId": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("예약 대기 생성 요청값이 유효한지 검증한다")
    void createReservationWaitingInvalidRequest() throws Exception {
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "date": "2026-08-06",
                                  "themeId": 1,
                                  "timeId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.status").value(400));
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
                id,
                "미술관의 밤",
                "추리 테마",
                "https://example.com/theme.png"
        );
    }

    private void createReservationTime(final long id) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                id,
                "10:00:00"
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
}
