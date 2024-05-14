package roomescape.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;
import roomescape.domain.ThemeName;
import roomescape.domain.ThemeRepository;

@JdbcTest
@Sql("/truncate.sql")
@Import(JdbcThemeRepository.class)
class JdbcThemeRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("테마를 저장한다.")
    @Test
    void shouldSaveTheme() {
        Theme theme = new Theme(new ThemeName("테마"), "테마 설명", "url");
        themeRepository.create(theme);
        int rowCount = getTotalRowCount();
        assertThat(rowCount).isEqualTo(1);
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void shouldFindAllThemes() {
        jdbcTemplate.update("insert into theme (name, description, thumbnail) values (?, ?, ?)",
                "테마", "테마 설명", "url");
        List<Theme> themes = themeRepository.findAll();
        assertThat(themes).hasSize(1);
    }

    @DisplayName("id로 테마를 조회한다.")
    @Test
    void shouldFindThemeById() {
        Theme theme = new Theme(new ThemeName("테마"), "테마 설명", "url");
        jdbcTemplate.update("insert into theme (id, name, description, thumbnail) values (?, ?, ?, ?)",
                1L, "테마", "테마 설명", "url");
        Optional<Theme> foundTheme = themeRepository.findById(1L);
        assertThat(foundTheme).isPresent();
    }

    @DisplayName("id로 테마를 삭제한다.")
    @Test
    void shouldDeleteThemeById() {
        Theme theme = new Theme(new ThemeName("테마"), "테마 설명", "url");
        jdbcTemplate.update("insert into theme (id, name, description, thumbnail) values (?, ?, ?, ?)",
                1L, "테마", "테마 설명", "url");
        themeRepository.deleteById(1L);

        int rowCount = getTotalRowCount();
        assertThat(rowCount).isZero();
    }

    private int getTotalRowCount() {
        String sql = "select count(*) from theme";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
