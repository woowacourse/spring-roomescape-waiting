package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRankResponse;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {
    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeService themeService;

    @Test
    @DisplayName("성공 : 테마를 조회할 수 있다.")
    void findThemes() {
        Theme theme = new Theme(1L, "그켬미", "켬미 방탈출", "thumbnail");
        when(themeRepository.findAll())
                .thenReturn(List.of(theme));

        List<ThemeResponse> actual = themeService.findThemes();

        assertThat(actual).containsExactly(ThemeResponse.from(theme));
    }

    @Test
    @DisplayName("성공 : 테마 랭킹만 조회할 수 있다.")
    void findRankedThemes() {
        Theme theme = new Theme(1L, "그켬미", "켬미 방탈출", "thumbnail");
        when(themeRepository.findThemesByReservationDateOrderByReservationCountDesc(
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(theme));

        List<ThemeRankResponse> actual = themeService.findRankedThemes();

        assertThat(actual).containsExactly(ThemeRankResponse.from(theme));
    }

    @Test
    @DisplayName("성공 : 테마를 추가할 수 있다.")
    void addTheme() {
        ThemeRequest request = new ThemeRequest("그켬미", "켬미 방탈출", "thumbnail");
        Theme theme = new Theme(1L, "그켬미", "켬미 방탈출", "thumbnail");
        when(themeRepository.save(any(Theme.class)))
                .thenReturn(theme);

        ThemeResponse actual = themeService.addTheme(request);

        assertThat(actual).isEqualTo(ThemeResponse.from(theme));
    }

    @Test
    @DisplayName("성공 : 테마를 삭제할 수 있다.")
    void removeTheme() {
        Theme theme = new Theme(1L, "그켬미", "켬미 방탈출", "thumbnail");
        Mockito.doNothing()
                .when(themeRepository)
                .deleteById(theme.getId());

        assertThatCode(() -> themeService.removeTheme(1L))
                .doesNotThrowAnyException();
    }
}
