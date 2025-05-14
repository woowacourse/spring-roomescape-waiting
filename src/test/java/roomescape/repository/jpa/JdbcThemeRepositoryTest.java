package roomescape.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.entity.Theme;
import roomescape.exception.custom.NotFoundException;

@JdbcTest
@Import(JdbcThemeRepository.class)
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcThemeRepository jdbcThemeDao;

    @Test
    @DisplayName("전체 테마를 조회할 수 있다.")
    void findAllThemes() {
        List<Theme> times = jdbcThemeDao.findAllThemes();

        assertThat(times).hasSize(3);
    }

    @Test
    @DisplayName("ID로 테마가 존재한다면 조회할 수 있다.")
    void findThemeById() {
        Theme theme = new Theme("이름", "설명", "썸네일");
        Theme actual = jdbcThemeDao.addTheme(theme);

        Theme expected = jdbcThemeDao.findThemeById(actual.getId());

        assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(expected.getId());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
            assertThat(actual.getThumbnail()).isEqualTo(expected.getThumbnail());
        });
    }

    @Test
    @DisplayName("ID로 테마가 존재하지 않는다면 예외가 발생한다.")
    void findThemeByNotId() {
        assertThatThrownBy(() -> jdbcThemeDao.findThemeById(100L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("theme");
    }

    @Test
    @DisplayName("일주일 동안의 인기 테마를 검색할 수 있다.")
    void findTopReservedThemesInPeriodWithLimit() {
        LocalDate date = LocalDate.of(2025, 4, 30);
        List<Theme> themes = jdbcThemeDao.findTopReservedThemesInPeriodWithLimit(
            date.minusDays(7), date, 2);

        assertAll(() -> {
            assertThat(themes.getFirst().getId()).isEqualTo(2);
            assertThat(themes.getLast().getId()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("해당 시간이 없다면 true를 반환한다.")
    void existThemeByName() {
        Theme theme = new Theme("이름", "설명", "썸네일");
        jdbcThemeDao.addTheme(theme);

        assertThat(jdbcThemeDao.existThemeByName(theme.getName())).isTrue();
    }

    @Test
    @DisplayName("해당 시간이 없다면 false를 반환한다.")
    void notExistThemeByName() {
        Theme theme = new Theme("이름", "설명", "썸네일");

        assertThat(jdbcThemeDao.existThemeByName(theme.getName())).isFalse();
    }

    @Test
    @DisplayName("테마를 추가할 수 있다.")
    void addTheme() {
        Theme theme = new Theme("이름", "설명", "썸네일");
        Theme newTheme = jdbcThemeDao.addTheme(theme);

        assertThat(newTheme).isNotNull();
    }

    @Test
    @DisplayName("ID로 테마를 삭제할 수 있다.")
    void removeTheme() {
        Theme theme = new Theme("이름", "설명", "썸네일");
        Theme newTheme = jdbcThemeDao.addTheme(theme);

        Long id = newTheme.getId();

        assertThatCode(() -> jdbcThemeDao.removeThemeById(id))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("해당 ID가 없다면 테마를 삭제할 수 없다.")
    void notRemoveTheme() {
        Theme theme = new Theme("이름", "설명", "썸네일");
        Theme newTheme = jdbcThemeDao.addTheme(theme);

        Long id = newTheme.getId();
        jdbcThemeDao.removeThemeById(id);

        assertThatThrownBy(() -> jdbcThemeDao.removeThemeById(id))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("theme");
    }
}
