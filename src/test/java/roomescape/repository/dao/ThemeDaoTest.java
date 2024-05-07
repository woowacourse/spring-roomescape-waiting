package roomescape.repository.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.model.theme.Theme;
import roomescape.service.dto.ThemeDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ThemeDaoTest {

    private static final int INITIAL_THEME_COUNT = 2;

    private final JdbcTemplate jdbcTemplate;
    private final ThemeDao themeDao;
    private final SimpleJdbcInsert themeInsertActor;

    @Autowired
    public ThemeDaoTest(JdbcTemplate jdbcTemplate, ThemeDao themeDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeDao = themeDao;
        this.themeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        insertTheme("n1", "d1", "t1");
        insertTheme("n2", "d2", "t2");
    }

    private void initDatabase() {
        jdbcTemplate.execute("ALTER TABLE theme SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE theme RESTART IDENTITY");
    }

    private void insertTheme(String name, String description, String thumbnail) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("name", name);
        parameters.put("description", description);
        parameters.put("thumbnail", thumbnail);
        themeInsertActor.execute(parameters);
    }

    @DisplayName("테마를 저장한다.")
    @Test
    void should_save_theme() {
        ThemeDto themeDto = new ThemeDto("n3", "d3", "t3");
        Theme theme = Theme.from(themeDto);
        themeDao.save(theme);
        assertThat(themeDao.findAll()).hasSize(INITIAL_THEME_COUNT + 1);
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void should_find_all_themes() {
        List<Theme> allThemes = themeDao.findAll();
        assertThat(allThemes).hasSize(INITIAL_THEME_COUNT);
    }

    @DisplayName("특정 id의 테마를 조회한다.")
    @Test
    void should_find_theme_by_id() {
        Optional<Theme> theme = themeDao.findById(1);
        assertThat(theme).isNotEmpty();
        assertThat(theme).hasValue(new Theme(1, "n1", "d1", "t1"));
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void should_delete_theme() {
        themeDao.deleteById(1);
        assertThat(themeDao.findAll()).hasSize(INITIAL_THEME_COUNT - 1);
    }
}
