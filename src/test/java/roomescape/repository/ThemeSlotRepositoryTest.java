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
import roomescape.domain.ThemeSlot;

@DataJpaTest
@Sql({"/schema.sql", "/test-data.sql"})
class ThemeSlotRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeSlotRepository themeSlotRepository;

    @Test
    @DisplayName("식별자로 ThemeSlot을 조회하며 row lock SQL을 실행한다.")
    void findByIdForUpdate() {
        jdbcTemplate.update(
                "INSERT INTO theme_slot (id, theme_id, date, time_id, is_reserved) VALUES (?, ?, ?, ?, ?)",
                1L,
                1L,
                LocalDate.parse("2026-05-29"),
                1L,
                true
        );

        ThemeSlot themeSlot = themeSlotRepository.findByIdForUpdate(1L).orElseThrow();

        assertThat(themeSlot.getId()).isEqualTo(1L);
        assertThat(themeSlot.getTheme().getId()).isEqualTo(1L);
        assertThat(themeSlot.getDate()).isEqualTo(LocalDate.parse("2026-05-29"));
    }

    @Test
    @DisplayName("여러 ThemeSlot을 식별자 순서대로 잠금 조회한다.")
    void findAllByIdsForUpdateInOrder() {
        insertThemeSlot(1L, LocalDate.parse("2026-05-29"), 1L);
        insertThemeSlot(2L, LocalDate.parse("2026-05-30"), 2L);

        List<ThemeSlot> themeSlots = themeSlotRepository.findAllByIdsForUpdateInOrder(2L, 1L);

        assertThat(themeSlots)
                .extracting(ThemeSlot::getId)
                .containsExactly(1L, 2L);
        assertThat(themeSlots)
                .extracting(themeSlot -> themeSlot.getTime().getId())
                .containsExactly(1L, 2L);
    }

    private void insertThemeSlot(Long id, LocalDate date, Long timeId) {
        jdbcTemplate.update(
                "INSERT INTO theme_slot (id, theme_id, date, time_id, is_reserved) VALUES (?, ?, ?, ?, ?)",
                id,
                1L,
                date,
                timeId,
                true
        );
    }
}
