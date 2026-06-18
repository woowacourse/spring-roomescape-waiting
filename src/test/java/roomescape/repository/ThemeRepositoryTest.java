package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;

@DataJpaTest
@Sql({"/schema.sql", "/test-data.sql"})
class ThemeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("인기 테마는 확정 또는 완료된 예약만 집계한다.")
    void findPopularThemesCountsOnlyConfirmedAndCompletedReservations() {
        LocalDate date = LocalDate.now();
        long theme1SlotId = saveThemeSlot(1L, date, 1L);
        long theme2SlotId = saveThemeSlot(2L, date, 2L);

        saveReservation("브라운", "CONFIRMED", theme1SlotId);
        saveReservation("김완료", "COMPLETED", theme1SlotId);
        saveWaiting("김대기", theme2SlotId);
        saveReservation("김취소", "CANCELLED", theme2SlotId);

        List<Theme> popularThemes = themeRepository.findPopularThemes(10L, date.minusDays(1), date.plusDays(1));

        assertThat(popularThemes)
                .extracting(Theme::getId)
                .containsExactly(1L);
    }

    private long saveThemeSlot(long themeId, LocalDate date, long timeId) {
        jdbcTemplate.update(
                "INSERT INTO theme_slot (theme_id, date, time_id, is_reserved) VALUES (?, ?, ?, ?)",
                themeId,
                date,
                timeId,
                true
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme_slot WHERE theme_id = ? AND date = ? AND time_id = ?",
                Long.class,
                themeId,
                date,
                timeId
        );
    }

    private void saveReservation(String name, String status, long themeSlotId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, status, theme_slot_id) VALUES (?, ?, ?)",
                name,
                status,
                themeSlotId
        );
    }

    private void saveWaiting(String name, long themeSlotId) {
        jdbcTemplate.update("""
                        INSERT INTO waiting (member_name, date, time_id, theme_id)
                        SELECT ?, date, time_id, theme_id
                        FROM theme_slot
                        WHERE id = ?
                        """,
                name,
                themeSlotId
        );
    }
}
