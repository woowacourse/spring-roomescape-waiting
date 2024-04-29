package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.theme.fixture.ThemeFixture.THEME_1;
import static roomescape.theme.fixture.ThemeFixture.THEME_2;
import static roomescape.theme.fixture.ThemeFixture.THEME_ADD_REQUEST;
import static roomescape.time.fixture.DateTimeFixture.SEVEN_DAYS_AGO;
import static roomescape.time.fixture.DateTimeFixture.TODAY;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.dto.ThemeResponse;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @InjectMocks
    private ThemeService themeService;

    @Mock
    private ThemeRepository themeRepository;

    @DisplayName("모든 테마를 조회하고 응답 형태로 반환할 수 있다")
    @Test
    void should_return_all_themes_as_responses() {
        when(themeRepository.findAll()).thenReturn(List.of(THEME_1));

        List<Theme> themes = themeService.findAllTheme();

        assertThat(themes).hasSize(1);
    }

    @DisplayName("인기 있는 테마를 조회하고 응답 형태로 반환할 수 있다")
    @Test
    void should_return_popular_themes_as_responses() {
        when(themeRepository.findByPeriodOrderByReservationCount(SEVEN_DAYS_AGO, TODAY, 10))
                .thenReturn(List.of(THEME_1, THEME_2));

        List<ThemeResponse> popularThemes = themeService.findPopularTheme();

        assertThat(popularThemes).contains(new ThemeResponse(THEME_1), new ThemeResponse(THEME_2));
    }

    @DisplayName("테마를 추가하고 응답을 반환할 수 있다")
    @Test
    void should_save_theme_when_requested() {
        when(themeRepository.save(any(Theme.class))).thenReturn(THEME_1);

        ThemeResponse savedTheme = themeService.saveTheme(THEME_ADD_REQUEST);

        assertThat(savedTheme).isEqualTo(new ThemeResponse(THEME_1));
    }
}
