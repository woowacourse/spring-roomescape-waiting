package roomescape.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Theme;

public class ThemeRepositoryTest extends RepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void createTest() {
        Theme themeWithoutId = new Theme("방탈출", "설명", "url.jpg");
        Theme theme = themeRepository.create(themeWithoutId);

        assertThat(theme.getId()).isEqualTo(1L);
    }

    @Test
    void readTest() {
        String sql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, "방탈출1", "방탈출1 설명", "url.jpg");

        Optional<Theme> theme = themeRepository.read(1L);

        assertThat(theme.orElseThrow().getId()).isEqualTo(1L);
    }

    @Test
    void readAllTest() {
        String sql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, "방탈출1", "방탈출1 설명", "url.jpg");
        jdbcTemplate.update(sql, "방탈출2", "방탈출2 설명", "url.jpg");

        List<Theme> themes = themeRepository.readAll();
        assertThat(themes.size()).isEqualTo(2);
    }

    @Test
    void readRankingTest() {
        String insertReservationTimeSql = "INSERT INTO `reservation_time` (`start_at`) VALUES (?)";
        jdbcTemplate.update(insertReservationTimeSql, "10:00");
        jdbcTemplate.update(insertReservationTimeSql, "11:00");

        String insertThemeSql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertThemeSql, "방탈출1", "방탈출1 설명", "url.jpg");
        jdbcTemplate.update(insertThemeSql, "방탈출2", "방탈출2 설명", "url.jpg");

        String insertReservationSql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(insertReservationSql, "fizz", "2026-05-02", 1L, 1L);
        jdbcTemplate.update(insertReservationSql, "fizz", "2026-05-02", 2L, 1L);
        jdbcTemplate.update(insertReservationSql, "fizz", "2026-05-02", 1L, 2L);

        List<Theme> themes = themeRepository.readRanking(LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 3), 2);

        assertThat(themes.get(0).getId()).isEqualTo(1L);
        assertThat(themes.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void deleteTest() {
        String sql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, "방탈출", "설명", "url.jpg");

        themeRepository.delete(1L);

        String readAllThemeCountSql = "SELECT COUNT(*) FROM `theme`";
        int count = jdbcTemplate.queryForObject(readAllThemeCountSql, Integer.class);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void existByIdTest() {
        assertThat(themeRepository.existById(1L)).isFalse();

        String sql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, "방탈출", "설명", "url.jpg");

        assertThat(themeRepository.existById(1L)).isTrue();
    }
}
