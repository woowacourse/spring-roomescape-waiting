package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.theme.Theme;
import roomescape.dto.theme.ThemeResponse;
import roomescape.repository.ThemeRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static roomescape.TestFixture.THEME_ANIME;
import static roomescape.TestFixture.THEME_COMIC;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {
    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeService themeService;

    @Test
    @DisplayName("테마를 생성한다.")
    void create() {
        // given
        final Theme expectedTheme = THEME_COMIC(1L);

        given(themeRepository.save(any())).willReturn(expectedTheme);

        // when
        final ThemeResponse actual = themeService.create(expectedTheme);

        // then
        assertThat(actual.id()).isEqualTo(expectedTheme.getId());
    }

    @Test
    @DisplayName("테마 목록을 조회한다.")
    void findAll() {
        // given
        final List<Theme> expectedThemes = List.of(THEME_COMIC(1L));

        given(themeRepository.findAll()).willReturn(expectedThemes);

        // when
        final List<ThemeResponse> actual = themeService.findAll();

        // then
        final ThemeResponse expectedResponse = ThemeResponse.from(THEME_COMIC(1L));
        assertThat(actual).hasSize(1)
                .containsExactly(expectedResponse);
    }

    @Test
    @DisplayName("Id로 테마를 조회한다.")
    void findById() {
        // given
        final Theme expectedTheme = THEME_COMIC(1L);

        given(themeRepository.findById(anyLong())).willReturn(Optional.of(expectedTheme));

        // when
        final ThemeResponse actual = themeService.findById(1L);

        // then
        assertAll(() -> {
            assertThat(actual.id()).isEqualTo(expectedTheme.getId());
            assertThat(actual.name()).isEqualTo(expectedTheme.getName());
        });
    }

    @Test
    @DisplayName("조회하려는 테마가 존재하지 않는 경우 예외가 발생한다.")
    void throwExceptionWhenNotExistTheme() {
        // given
        given(themeRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> themeService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void deleteById() {
        // given
        final Theme theme = THEME_COMIC(1L);

        given(themeRepository.findById(anyLong())).willReturn(Optional.of(theme));

        // when & then
        assertThatCode(() -> themeService.deleteById(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("삭제하려는 테마가 존재하지 않는 경우 예외가 발생한다.")
    void throwExceptionWhenDeleteNotExistingTheme() {
        // given
        given(themeRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> themeService.deleteById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("인기 테마 목록을 조회한다.")
    void findAllPopular() {
        // given
        final List<Theme> expectedThemes = List.of(THEME_COMIC(1L), THEME_ANIME(2L));
        given(themeRepository.findPopularThemesBy(any(), any())).willReturn(expectedThemes);

        // when
        final List<ThemeResponse> actual = themeService.findPopularThemes();

        // then
        final ThemeResponse expectedTheme1 = ThemeResponse.from(THEME_COMIC(1L));
        final ThemeResponse expectedTheme2 = ThemeResponse.from(THEME_ANIME(2L));
        assertThat(actual).hasSize(2)
                .containsExactly(expectedTheme1, expectedTheme2);
    }
}
