package roomescape.theme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.schedule.application.ScheduleService;
import roomescape.theme.application.ThemeService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ThemeServiceTest {
    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ThemeService themeService;

    @Test
    @DisplayName("스케줄에 테마에 대한 참조가 존재하면 테마 삭제에 실패한다.")
    void delete_실패_테스트_1() {
        // given
        long themeId = 1L;
        doThrow(new EscapeRoomException(ErrorCode.SCHEDULE_THEME_IN_USE)).when(scheduleService).validateThemeDeletable(themeId);

        // when, then
        assertThatThrownBy(() -> themeService.delete(themeId))
                .isInstanceOfSatisfying(EscapeRoomException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_THEME_IN_USE)
                );

        verify(themeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("스케줄에 테마에 대한 참조가 존재하지 않으면 테마 삭제에 성공한다.")
    void delete_성공_테스트() {
        // given
        long themeId = 1L;

        // when
        themeService.delete(themeId);

        // then
        verify(themeRepository).deleteById(anyLong());
    }

}
