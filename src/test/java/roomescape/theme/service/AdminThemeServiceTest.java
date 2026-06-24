package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.theme.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class AdminThemeServiceTest {

    @Mock private ThemeRepository themeRepository;

    @InjectMocks
    private AdminThemeService adminThemeService;

    @Test
    @DisplayName("예약이 존재하는 테마는 삭제할 수 없다")
    void 예약_있는_테마_삭제_불가() {
        when(themeRepository.hasReservation(1L)).thenReturn(true);

        assertThatThrownBy(() -> adminThemeService.deleteTheme(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.THEME_HAS_RESERVATION))
                .hasMessage(ErrorCode.THEME_HAS_RESERVATION.getMessage());
    }
}
