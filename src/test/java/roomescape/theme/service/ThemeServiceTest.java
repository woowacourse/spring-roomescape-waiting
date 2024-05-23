package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.theme.fixture.ThemeFixture.THEME_1;
import static roomescape.theme.fixture.ThemeFixture.THEME_2;
import static roomescape.theme.fixture.ThemeFixture.THEME_ADD_REQUEST;
import static roomescape.time.fixture.DateTimeFixture.SEVEN_DAYS_AGO;
import static roomescape.time.fixture.DateTimeFixture.TODAY;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import roomescape.global.exception.IllegalRequestException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.dto.ThemeResponse;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @InjectMocks
    private ThemeService themeService;

    @Mock
    private ThemeRepository themeRepository;

    @DisplayName("아이디로 단건 조회 시 존재하지 않는 아이디이면 예외가 발생한다")
    @Test
    void should_throw_exception_when_find_with_non_exist_id() {
        when(themeRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.findById(1L))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("아이디로 단건 조회 시 존재하지 않는 아이디이면 예외가 발생한다")
    @Test
    void should_find_theme_with_exist_id() {
        when(themeRepository.findById(any(Long.class))).thenReturn(Optional.of(THEME_1));

        assertThat(themeService.findById(1L)).isEqualTo(THEME_1);
    }

    @DisplayName("모든 테마를 조회하고 응답 형태로 반환할 수 있다")
    @Test
    void should_return_all_themes_as_responses() {
        when(themeRepository.findAll()).thenReturn(List.of(THEME_1));

        List<ThemeResponse> themes = themeService.findAllTheme();

        assertThat(themes).hasSize(1);
    }

    @DisplayName("인기 있는 테마를 조회하고 응답 형태로 반환할 수 있다")
    @Test
    void should_return_popular_themes_as_responses() {
        Pageable pageRequest = PageRequest.of(0, 10);
        when(themeRepository.findTopByDurationAndCount(SEVEN_DAYS_AGO, TODAY, pageRequest))
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
