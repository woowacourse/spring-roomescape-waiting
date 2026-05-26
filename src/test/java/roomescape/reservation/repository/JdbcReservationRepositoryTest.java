package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findTimeIdsByThemeIdAndDate() {
        Long timeId1 = insertTime("2026-05-06 10:00:00", "2026-05-06 12:00:00");
        Long timeId2 = insertTime("2026-05-06 13:00:00", "2026-05-06 15:00:00");

        LocalDate date = LocalDate.of(2026, 5, 6);
        Long themeId = insertTheme("테마");
        insertReservation("윤호준", timeId1, themeId);
        insertReservation("박다혜", timeId2, themeId);

        assertThat(reservationRepository.findTimeIdsByThemeIdAndDate(themeId, date))
                .containsExactly(timeId1, timeId2);

        assertThat(reservationRepository.findTimeIdsByThemeIdAndDate(themeId, date.plusDays(1)))
                .isEmpty();
    }

    @Test
    void findTimeIdsByThemeIdAndDate_다른테마의_예약시간은_조회하지_않는다() {
        Long timeId1 = insertTime("2026-05-06 10:00:00", "2026-05-06 12:00:00");
        Long timeId2 = insertTime("2026-05-06 13:00:00", "2026-05-06 15:00:00");

        LocalDate date = LocalDate.of(2026, 5, 6);
        Long themeId = insertTheme("테마1");
        Long otherThemeId = insertTheme("테마2");
        insertReservation("윤호준", timeId1, themeId);
        insertReservation("박다혜", timeId2, otherThemeId);

        assertThat(reservationRepository.findTimeIdsByThemeIdAndDate(themeId, date))
                .containsExactly(timeId1);
    }

    private Long insertTime(String startAt, String endAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_time, end_time) VALUES (?, ?)",
                startAt,
                endAt
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_time = ?",
                Long.class,
                startAt
        );
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES (?, ?, ?)",
                name,
                "설명",
                "https://example.com/theme.png"
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?",
                Long.class,
                name
        );
    }

    private void insertReservation(String name, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, time_id, theme_id) VALUES (?, ?, ?)",
                name,
                timeId,
                themeId
        );
    }
}
