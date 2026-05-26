package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeSortType;

@JdbcTest
@Import({
        ThemeJdbcTemplateRepository.class,
        PopularThemeJdbcTemplateRepository.class
})
class PopularThemeJdbcTemplateRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private final ThemeJdbcTemplateRepository themeRepository;
    private final PopularThemeJdbcTemplateRepository popularThemeRepository;

    @Autowired
    public PopularThemeJdbcTemplateRepositoryTest(
            JdbcTemplate jdbcTemplate,
            ThemeJdbcTemplateRepository themeRepository,
            PopularThemeJdbcTemplateRepository popularThemeRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeRepository = themeRepository;
        this.popularThemeRepository = popularThemeRepository;
    }

    @Test
    @DisplayName("기간 내 예약 수가 많은 테마를 상위 N개 조회한다")
    void findTopNByPeriod_success() {
        // given
        Theme theme1 = themeRepository.save(
                Theme.create("인기 테마", "설명1", "thumb1")
        );
        Theme theme2 = themeRepository.save(
                Theme.create("덜 인기 테마", "설명2", "thumb2")
        );

        jdbcTemplate.update(
                "INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                1L,
                "10:00"
        );
        jdbcTemplate.update(
                "INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                2L,
                "11:00"
        );
        jdbcTemplate.update(
                "INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                3L,
                "12:00"
        );

        jdbcTemplate.update("""
                INSERT INTO reservation (name, date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """, "예약자1", LocalDate.of(2026, 5, 1), 1L, theme1.getId());

        jdbcTemplate.update("""
                INSERT INTO reservation (name, date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """, "예약자2", LocalDate.of(2026, 5, 2), 2L, theme1.getId());

        jdbcTemplate.update("""
                INSERT INTO reservation (name, date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """, "예약자3", LocalDate.of(2026, 5, 3), 3L, theme2.getId());

        // when
        List<Theme> result = popularThemeRepository.findTopNByPeriod(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 7),
                ThemeSortType.POPULAR,
                2L
        );

        // then
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(theme1.getId(), result.get(0).getId());
        Assertions.assertEquals(theme2.getId(), result.get(1).getId());
    }
}