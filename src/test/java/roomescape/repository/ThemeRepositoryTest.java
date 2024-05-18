package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemePopularFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.THEME_DETECTIVE;
import static roomescape.TestFixture.THEME_HORROR;

class ThemeRepositoryTest extends RepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    private Theme theme;

    @BeforeEach
    void setUp() {
        theme = themeRepository.save(THEME_DETECTIVE());
    }

    @Test
    @DisplayName("테마를 저장한다.")
    void save() {
        // given
        final Theme theme = THEME_HORROR();

        // when
        final Theme actual = themeRepository.save(theme);

        // then
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    @DisplayName("Id에 해당하는 테마를 조회한다.")
    void findById() {
        // when
        final Optional<Theme> actual = themeRepository.findById(theme.getId());

        // then
        assertThat(actual).isNotEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 Id로 테마를 조회하면 빈 옵셔널을 반환한다.")
    void returnEmptyOptionalWhenFindByNotExistingId() {
        // given
        final Long notExistingId = 0L;

        // when
        final Optional<Theme> actual = themeRepository.findById(notExistingId);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Id에 해당하는 테마를 삭제한다.")
    void deleteById() {
        // when
        themeRepository.deleteById(theme.getId());

        // then
        final List<Theme> actual = themeRepository.findAll();
        assertThat(actual).doesNotContain(theme);
    }

    @Sql("/popular-theme-data.sql")
    @Test
    @DisplayName("인기 테마 목록을 조회한다.")
    void findPopularThemes() {
        // given
        final ThemePopularFilter filter
                = ThemePopularFilter.toThemePopularFilter(LocalDate.parse("2034-05-12"));

        // when
        final int limit = filter.getLimit();
        final List<Theme> actual = themeRepository.findPopularThemesBy(filter);

        // then
        assertThat(actual).hasSize(limit);
    }
}
