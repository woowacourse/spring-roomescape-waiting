package roomescape.theme.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.theme.business.domain.Theme;
import roomescape.theme.business.repository.ThemeDao;

@JdbcTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcThemeDaoTest {

    private ThemeDao themeDao;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        themeDao = new JdbcThemeDao(jdbcTemplate);
    }

    @Test
    void 테마를_저장한다() {
        final Theme theme = new Theme("인터스텔라", "인터스텔라 방탈출", "link");

        final Theme saved = themeDao.save(theme);

        final Theme expected = new Theme(4L, "인터스텔라", "인터스텔라 방탈출", "link");
        assertThat(saved).isEqualTo(expected);
    }

    @Test
    void 모든_테마를_조회한다() {
        final List<Theme> allThemes = themeDao.findAll();

        assertThat(allThemes).hasSize(3);
    }

    @Test
    void 테마를_삭제한다() {
        themeDao.save(new Theme("인셉션", "인셉션 방탈출", "link"));

        themeDao.deleteById(4L);

        final List<Theme> allThemes = themeDao.findAll();
        assertThat(allThemes).hasSize(3);
    }

    @Test
    void 최근_일주일간_인기있는_테마를_10개까지_조회한다() {
        final List<Theme> themes = themeDao.sortByRank();

        final List<Object> expected = List.of(
                new Theme(2L, "", "", ""),
                new Theme(1L, "", "", ""),
                new Theme(3L, "", "", "")
        );
        assertThat(themes).isEqualTo(expected);
    }

    @Test
    void 동일한_이름의_테마를_확인한다() {
        assertAll(
                () -> assertThat(themeDao.existByName("인터스텔라")).isTrue(),
                () -> assertThat(themeDao.existByName("고질라")).isFalse()
        );
    }
}
