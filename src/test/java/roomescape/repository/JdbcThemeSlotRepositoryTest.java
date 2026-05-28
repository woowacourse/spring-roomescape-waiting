package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ThemeSlot;

@JdbcTest
@Sql({"/schema.sql", "/test-data.sql"})
class JdbcThemeSlotRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcThemeSlotRepository jdbcThemeSlotRepository;

    @BeforeEach
    void setUp() {
        jdbcThemeSlotRepository = new JdbcThemeSlotRepository(jdbcTemplate);
    }

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

        ThemeSlot themeSlot = jdbcThemeSlotRepository.findByIdForUpdate(1L).orElseThrow();

        assertThat(themeSlot.getId()).isEqualTo(1L);
        assertThat(themeSlot.getTheme().getId()).isEqualTo(1L);
        assertThat(themeSlot.getDate()).isEqualTo(LocalDate.parse("2026-05-29"));
    }
}
