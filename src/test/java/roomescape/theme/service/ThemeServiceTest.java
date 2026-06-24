package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    private final Clock fixedClock = Clock.fixed(
            LocalDate.now().atTime(14, 0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private Clock clock;
    @InjectMocks
    private ThemeService themeService;

    @Test
    @DisplayName("인기 테마 조회")
    void 인기_테마_조회() {
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        LocalDate endDate = LocalDate.now(fixedClock);
        LocalDate startDate = endDate.minusDays(7);
        List<Long> themeIds = List.of(1L, 2L, 3L);
        Theme theme1 = Theme.restore(1L, "테마A", "설명A", "https://a.com", 10000);
        Theme theme2 = Theme.restore(2L, "테마B", "설명B", "https://b.com", 20000);
        Theme theme3 = Theme.restore(3L, "테마C", "설명C", "https://c.com", 30000);

        when(themeRepository.findTopThemeIds(startDate, endDate, 10)).thenReturn(themeIds);
        when(themeRepository.findAllByIds(themeIds)).thenReturn(List.of(theme1, theme2, theme3));

        List<ThemeResponse> result = themeService.getTopThemes(10);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("테마A");
        assertThat(result.get(1).name()).isEqualTo("테마B");
        assertThat(result.get(2).name()).isEqualTo("테마C");
    }

    @Test
    @DisplayName("인기 테마 조회 limit 적용")
    void 인기_테마_조회_limit_적용() {
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        LocalDate endDate = LocalDate.now(fixedClock);
        LocalDate startDate = endDate.minusDays(7);
        List<Long> themeIds = List.of(1L, 2L);
        Theme theme1 = Theme.restore(1L, "테마A", "설명A", "https://a.com", 10000);
        Theme theme2 = Theme.restore(2L, "테마B", "설명B", "https://b.com", 20000);

        when(themeRepository.findTopThemeIds(startDate, endDate, 2)).thenReturn(themeIds);
        when(themeRepository.findAllByIds(themeIds)).thenReturn(List.of(theme1, theme2));

        List<ThemeResponse> result = themeService.getTopThemes(2);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 id로 테마 조회 시 예외 발생")
    void getById_없으면_예외() {
        when(themeRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.getById(Long.MAX_VALUE))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.THEME_NOT_FOUND))
                .hasMessage(ErrorCode.THEME_NOT_FOUND.getMessage());
    }
}
