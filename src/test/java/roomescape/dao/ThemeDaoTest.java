package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.domain.Theme;

@JdbcTest
@Import(ThemeDao.class)
class ThemeDaoTest {

    private static final int DEFAULT_THEME_COUNT = 15;
    private static final Long AVAILABLE_THEME_ID = 1L;

    @Autowired
    private ThemeDao themeDao;

    @Test
    void 전체_테마_조회() {
        List<Theme> themes = themeDao.findAllThemes();

        assertThat(themes).hasSize(DEFAULT_THEME_COUNT);
    }

    @Test
    void ID로_테마_조회() {
        Theme theme = themeDao.findThemeById(AVAILABLE_THEME_ID);

        assertThat(theme).isNotNull();
        assertThat(theme.getId()).isEqualTo(1L);
        assertThat(theme.getName()).isEqualTo("우테코 공포물");
    }

    @Test
    void 인기_테마_상위_3개_조회() {
        List<Theme> topThemes = themeDao.findTopThemes(3L);

        assertThat(topThemes).hasSize(3);
        assertThat(topThemes.get(0).getName()).isEqualTo("우테코 공포물");
        assertThat(topThemes.get(1).getName()).isEqualTo("미래 도시");
        assertThat(topThemes.get(2).getName()).isEqualTo("고대 이집트");
    }

    @Test
    void 테마_저장() {
        Theme newTheme = new Theme(null, "새 테마", "새로 추가된 테마", "/new-theme");

        Theme saved = themeDao.save(newTheme);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("새 테마");
        assertThat(themeDao.findAllThemes()).hasSize(DEFAULT_THEME_COUNT + 1);
    }

    @Test
    void 예약_없는_테마_삭제() {
        Long themeId = 11L;

        themeDao.delete(themeId);

        assertThatThrownBy(() -> themeDao.findThemeById(themeId))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
