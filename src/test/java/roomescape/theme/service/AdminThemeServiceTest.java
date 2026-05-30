package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFactory;
import roomescape.theme.dto.AdminThemeRequest;
import roomescape.theme.dto.AdminThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class AdminThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ThemeFactory themeFactory;

    @InjectMocks
    private AdminThemeService adminThemeService;

    private final Theme theme = Theme.restore(1L, "테마A", "설명", "https://a.com");

    @Test
    @DisplayName("테마 생성 성공")
    void 테마_생성_성공() {
        when(themeFactory.create(any(), any(), any())).thenReturn(theme);
        when(themeRepository.save(any())).thenReturn(theme);

        AdminThemeResponse response = adminThemeService.createTheme(new AdminThemeRequest("테마A", "설명", "https://a.com"));
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("전체 테마 조회")
    void 전체_테마_조회() {
        when(themeRepository.findAll()).thenReturn(List.of(theme));

        assertThat(adminThemeService.getAllThemes()).hasSize(1);
    }

    @Test
    @DisplayName("테마 삭제 성공")
    void 테마_삭제_성공() {
        when(themeRepository.existsReservationByThemeId(1L)).thenReturn(false);

        adminThemeService.deleteTheme(1L);
        verify(themeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("예약이 존재하는 테마는 삭제할 수 없다")
    void 예약_있는_테마_삭제_불가() {
        when(themeRepository.existsReservationByThemeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> adminThemeService.deleteTheme(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.THEME_HAS_RESERVATION));
    }
}