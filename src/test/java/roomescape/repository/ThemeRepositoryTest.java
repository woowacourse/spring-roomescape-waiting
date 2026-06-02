package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Theme;
import roomescape.repository.dto.PopularThemeResult;

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
        Long id = themeRepository.insert(theme);

        // then
        List<Theme> themes = themeRepository.findAll();
        Theme savedTheme = themeRepository.findById(id).get();
        assertAll(
                () -> assertThat(id).isNotNull(),
                () -> assertThat(themes).hasSize(1),
                () -> assertThat(savedTheme.getName()).isEqualTo(theme.getName()));
    }

    @Test
    void 예약_삭제_테스트() {
        // given
        Theme theme1 = new Theme(null, "새로운 테마1", "새로운 테마 설명1", "새로운 썸네일 링크1");
        Theme theme2 = new Theme(null, "새로운 테마2", "새로운 테마 설명2", "새로운 썸네일 링크2");
        Long id1 = themeRepository.insert(theme1);
        Long id2 = themeRepository.insert(theme2);

        // when
        int deletedCount = themeRepository.delete(id1);

        // then
        List<Theme> themes = themeRepository.findAll();
        assertAll(
                () -> assertThat(deletedCount).isEqualTo(1),
                () -> assertThat(themes).hasSize(1),
                () -> assertThat(themeRepository.findById(id1)).isEmpty());
    }

    @Test
    void 인기_테마를_조회한다() {
        // given
        Long themeId1 = themeRepository.insert(new Theme(null, "테마1", "설명1", "썸네일1"));
        Long themeId2 = themeRepository.insert(new Theme(null, "테마2", "설명2", "썸네일2"));

        jdbcTemplate.update(
                "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", LocalDate.of(2026, 5, 1), 1L, themeId1);
        jdbcTemplate.update(
                "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "구구", LocalDate.of(2026, 5, 1), 2L, themeId2);
        jdbcTemplate.update(
                "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "포비", LocalDate.of(2026, 5, 2), 3L, themeId2);

        // when
        List<PopularThemeResult> result = themeRepository.findPopular(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                2);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).id()).isEqualTo(themeId2),
                () -> assertThat(result.get(0).name()).isEqualTo("테마2"),
                () -> assertThat(result.get(0).description()).isEqualTo("설명2"),
                () -> assertThat(result.get(0).thumbnail()).isEqualTo("썸네일2"),
                () -> assertThat(result.get(0).reservationCount()).isEqualTo(2),
                () -> assertThat(result.get(1).id()).isEqualTo(themeId1),
                () -> assertThat(result.get(1).reservationCount()).isEqualTo(1));
    }
}
