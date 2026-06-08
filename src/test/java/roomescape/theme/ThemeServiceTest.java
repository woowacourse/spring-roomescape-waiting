package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.slot.application.SlotUsageValidator;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.port.out.ThemeRepository;

@ExtendWith(MockitoExtension.class)
public class ThemeServiceTest {
    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private SlotUsageValidator slotUsageValidator;

    @InjectMocks
    private ThemeService themeService;

    @Test
    @DisplayName("슬롯에 테마에 대한 참조가 존재하면 테마 삭제에 실패한다.")
    void theme_referenced_by_slot_cannot_be_deleted() {
        // given
        long themeId = 1L;
        doThrow(new IllegalStateException()).when(slotUsageValidator).validateThemeDeletable(themeId);

        // when, then
        assertThatThrownBy(() -> themeService.delete(themeId))
                .isInstanceOf(IllegalStateException.class);

        verify(themeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("슬롯에 테마에 대한 참조가 존재하지 않으면 테마 삭제에 성공한다.")
    void unreferenced_theme_is_deleted_successfully() {
        // given
        long themeId = 1L;

        // when
        themeService.delete(themeId);

        // then
        verify(themeRepository).deleteById(anyLong());
    }

}
