package roomescape.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Theme;

@JdbcTest
public class JdbcThemeRepositoryTest {

    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        themeRepository = new JdbcThemeRepository(jdbcTemplate);
    }

    @AfterEach
    void afterEach() {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";
        List<String> tableNames = jdbcTemplate.queryForList(sql, String.class);

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String tableName : tableNames) {
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1");
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    void saveTest() {
        Theme themeWithoutId = new Theme("방탈출", "설명", "url.jpg");
        Theme theme = themeRepository.save(themeWithoutId);

        assertThat(theme.getId()).isEqualTo(1L);
    }

    @Test
    void findByIdTest() {
        String sql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, "방탈출1", "방탈출1 설명", "url.jpg");

        Optional<Theme> theme = themeRepository.findById(1L);

        assertThat(theme.orElseThrow().getId()).isEqualTo(1L);
    }

    @Test
    void findAllTest() {
        String sql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, "방탈출1", "방탈출1 설명", "url.jpg");
        jdbcTemplate.update(sql, "방탈출2", "방탈출2 설명", "url.jpg");

        List<Theme> themes = themeRepository.findAll();
        assertThat(themes.size()).isEqualTo(2);
    }

    @Test
    void findRankingTest() {
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

        List<Theme> themes = themeRepository.findRanking(LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 3), 2);

        assertThat(themes.get(0).getId()).isEqualTo(1L);
        assertThat(themes.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void deleteByIdTest() {
        String sql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, "방탈출", "설명", "url.jpg");

        themeRepository.deleteById(1L);

        String readAllThemeCountSql = "SELECT COUNT(*) FROM `theme`";
        int count = jdbcTemplate.queryForObject(readAllThemeCountSql, Integer.class);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void existsByIdTest() {
        assertThat(themeRepository.existsById(1L)).isFalse();

        String sql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, "방탈출", "설명", "url.jpg");

        assertThat(themeRepository.existsById(1L)).isTrue();
    }
}
