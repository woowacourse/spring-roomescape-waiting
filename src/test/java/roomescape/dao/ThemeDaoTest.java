package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Theme;

@JdbcTest
@Import(ThemeDao.class)
class ThemeDaoTest {

    @Autowired
    private ThemeDao themeDao;

    @Test
    void 전체_테마_조회() {
        List<Theme> themes = themeDao.findAllThemes();

        assertThat(themes).hasSize(15);
    }

    @Test
    void ID로_테마_조회() {
        Optional<Theme> theme = themeDao.findThemeById(1L);

        assertThat(theme).isNotNull();
        assertThat(theme.get().getId()).isEqualTo(1L);
        assertThat(theme.get().getName()).isEqualTo("우테코 공포물");
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
        assertThat(themeDao.findAllThemes()).hasSize(16);
    }

    @Test
    void 예약_없는_테마_삭제() {
        Long themeId = 11L;

        themeDao.delete(themeId);

        assertThat(themeDao.findThemeById(themeId)).isEmpty();
    }
}
