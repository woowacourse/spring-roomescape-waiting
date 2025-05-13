package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeName;

@JdbcTest
@Import(JdbcThemeDao.class)
@Sql({"/test-schema.sql", "/test-theme-data.sql"})
class JdbcThemeDaoTest {

    @Autowired
    private JdbcThemeDao jdbcThemeDao;

    @DisplayName("테마를 저장할 수 있다.")
    @Test
    void testSave() {
        // given
        Theme theme = new Theme("테마 이름", "테마 설명", "테마 썸네일");
        // when
        Theme savedTheme = jdbcThemeDao.save(theme);
        // then
        assertAll(
                () -> assertThat(savedTheme.getId()).isEqualTo(4L),
                () -> assertThat(jdbcThemeDao.findById(savedTheme.getId()).orElseThrow().getName()).isEqualTo("테마 이름")
        );
    }

    @DisplayName("모든 테마를 불러올 수 있다.")
    @Test
    void testFindAll() {
        // when
        List<Theme> actual = jdbcThemeDao.findAll();
        // then
        assertThat(actual).hasSize(3);
    }

    @DisplayName("ID로 특정 테마를 불러올 수 있다.")
    @Test
    void testFindById() {
        // when
        Theme actual = jdbcThemeDao.findById(2L).orElseThrow();
        // then
        assertThat(actual.getName()).isEqualTo("테마2");
    }

    @DisplayName("기간과 개수를 입력 받아 해당하는 인기 테마 목록을 불러올 수 있다.")
    @Test
    @Sql({"/test-schema.sql", "/test-popular-theme-data.sql"})
    void testFindPopularThemes() {
        // given
        LocalDate from = LocalDate.of(2025, 5, 1);
        LocalDate to = LocalDate.of(2025, 5, 3);
        int count = 2;
        // when
        List<Theme> popularThemes = jdbcThemeDao.findPopularThemes(from, to, count);
        // then
        assertAll(
                () -> assertThat(popularThemes).hasSize(2),
                () -> assertThat(popularThemes.getFirst().getName()).isEqualTo("테마1"),
                () -> assertThat(popularThemes.get(1).getName()).isEqualTo("테마2")
        );
    }

    @DisplayName("ID로 특정 테마를 삭제할 수 있다.")
    @Test
    void testDeleteById() {
        // when
        jdbcThemeDao.deleteById(2L);
        // then
        List<Theme> actualThemes = jdbcThemeDao.findAll();
        Condition<Theme> idIs2L = new Condition<>(theme -> theme.getId() == 2L, "Id is 2L");
        assertThat(actualThemes).hasSize(2);
        assertThat(actualThemes).doNotHave(idIs2L);
    }

    @DisplayName("특정 테마 이름이 존재하는지 확인할 수 있다.")
    @Test
    void testExistByTime() {
        // when
        boolean actual = jdbcThemeDao.existsByName(new ThemeName("테마1"));
        // then
        assertThat(actual).isTrue();
    }
}
