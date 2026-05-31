package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.business.BusinessException;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeService themeService;

    private Theme themeA;
    private Theme themeB;
    private Theme themeC;

    @BeforeEach
    void setUp() {
        themeA = Theme.restore(1L, "테마A", "설명A", "https://a.com");
        themeB = Theme.restore(2L, "테마B", "설명B", "https://b.com");
        themeC = Theme.restore(3L, "테마C", "설명C", "https://c.com");
    }

    @Test
    @DisplayName("인기 테마 조회")
    void 인기_테마_조회() {
        when(themeRepository.findTopThemeIds(any(), any(), anyInt())).thenReturn(List.of(1L, 2L, 3L));
        when(themeRepository.findAllByIds(List.of(1L, 2L, 3L))).thenReturn(List.of(themeA, themeB, themeC));

        List<ThemeResponse> result = themeService.getTopThemes(10);
        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("테마A");
        assertThat(result.get(1).name()).isEqualTo("테마B");
        assertThat(result.get(2).name()).isEqualTo("테마C");
    }

    @Test
    @DisplayName("인기 테마 조회 limit 적용")
    void 인기_테마_조회_limit_적용() {
        when(themeRepository.findTopThemeIds(any(), any(), anyInt())).thenReturn(List.of(1L, 2L));
        when(themeRepository.findAllByIds(List.of(1L, 2L))).thenReturn(List.of(themeA, themeB));

        assertThat(themeService.getTopThemes(2)).hasSize(2);
    }

    @Test
    @DisplayName("id로 테마 조회 성공")
    void getById_성공() {
        when(themeRepository.findById(1L)).thenReturn(Optional.of(themeA));

        assertThat(themeService.getById(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 id로 테마 조회 시 예외 발생")
    void getById_없으면_예외() {
        when(themeRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.getById(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }
}
