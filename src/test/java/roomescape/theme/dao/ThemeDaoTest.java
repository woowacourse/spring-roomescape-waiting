package roomescape.theme.dao;

import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.theme.domain.Theme;

@JdbcTest
@Sql(scripts = "/data-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class ThemeDaoTest {

    private final ThemeJdbcDao themeJdbcDao;

    @Autowired
    public ThemeDaoTest(DataSource dataSource) {
        this.themeJdbcDao = new ThemeJdbcDao(dataSource);
    }

    @Test
    @DisplayName("테마 정보가 DB에 정상적으로 저장되는지 확인한다.")
    void saveTheme() {
        Theme theme = Theme.saveThemeOf("포레스트", "공포 테마", "thumbnail");
        themeJdbcDao.save(theme);

        Assertions.assertThat(theme.getId()).isEqualTo(4);
    }

    @Test
    @DisplayName("테마 정보들을 정상적으로 가져오는지 확인한다.")
    void getThemes() {
        List<Theme> themes = themeJdbcDao.findAllThemes();

        Assertions.assertThat(themes.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("테마 정보들이 정상적으로 제거되었는지 확인한다.")
    void deleteThemes() {
        themeJdbcDao.deleteById(3L);

        Assertions.assertThat(themeJdbcDao.findAllThemes().size()).isEqualTo(2);
    }
}
