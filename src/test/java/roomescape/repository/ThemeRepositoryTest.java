package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.PopularTheme;
import roomescape.domain.PopularThemeCondition;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
class ThemeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ThemeRepository themeRepository;

    @BeforeEach
    void setup() {
        this.themeRepository = new ThemeRepository(jdbcTemplate);
        jdbcTemplate.update("DELETE FROM reservation_waiting;");
        jdbcTemplate.update("DELETE FROM reservation;");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    void 테마_추가_테스트() {
        // given
        Theme theme = new Theme(null, "새로운 테마", "새로운 테마 설명", "새로운 썸네일 링크");

        // when
        Theme result = themeRepository.insert(theme);

        // then
        List<Theme> themes = themeRepository.findAll();
        Theme savedTheme = themeRepository.findById(result.getId()).get();
        assertAll(
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getName()).isEqualTo(theme.getName()),
                () -> assertThat(result.getDescription()).isEqualTo(theme.getDescription()),
                () -> assertThat(result.getThumbnail()).isEqualTo(theme.getThumbnail()),
                () -> assertThat(themes).hasSize(1),
                () -> assertThat(savedTheme.getName()).isEqualTo(theme.getName()));
    }

    @Test
    void 예약_삭제_테스트() {
        // given
        Theme theme1 = new Theme(null, "새로운 테마1", "새로운 테마 설명1", "새로운 썸네일 링크1");
        Theme theme2 = new Theme(null, "새로운 테마2", "새로운 테마 설명2", "새로운 썸네일 링크2");
        Theme savedTheme1 = themeRepository.insert(theme1);
        Theme savedTheme2 = themeRepository.insert(theme2);

        // when
        int deletedCount = themeRepository.delete(savedTheme1.getId());

        // then
        List<Theme> themes = themeRepository.findAll();
        assertAll(
                () -> assertThat(deletedCount).isEqualTo(1),
                () -> assertThat(themes).hasSize(1),
                () -> assertThat(themeRepository.findById(savedTheme1.getId())).isEmpty(),
                () -> assertThat(themeRepository.findById(savedTheme2.getId())).isPresent());
    }

    @Test
    void 인기_테마를_조회한다() {
        // given
        Theme theme1 = themeRepository.insert(new Theme(null, "테마1", "설명1", "썸네일1"));
        Theme theme2 = themeRepository.insert(new Theme(null, "테마2", "설명2", "썸네일2"));

        jdbcTemplate.update(
                "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", LocalDate.of(2026, 5, 1), 1L, theme1.getId());
        jdbcTemplate.update(
                "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "구구", LocalDate.of(2026, 5, 1), 2L, theme2.getId());
        jdbcTemplate.update(
                "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "포비", LocalDate.of(2026, 5, 2), 3L, theme2.getId());

        // when
        PopularThemeCondition condition = new PopularThemeCondition(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                2);

        List<PopularTheme> result = themeRepository.findPopular(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).getTheme().getId()).isEqualTo(theme2.getId()),
                () -> assertThat(result.get(0).getTheme().getName()).isEqualTo("테마2"),
                () -> assertThat(result.get(0).getTheme().getDescription()).isEqualTo("설명2"),
                () -> assertThat(result.get(0).getTheme().getThumbnail()).isEqualTo("썸네일2"),
                () -> assertThat(result.get(0).getReservationCount()).isEqualTo(2),
                () -> assertThat(result.get(1).getTheme().getId()).isEqualTo(theme1.getId()),
                () -> assertThat(result.get(1).getReservationCount()).isEqualTo(1));
    }
}
