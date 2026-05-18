package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.theme.Theme;
import roomescape.theme.dao.ThemeDao;

@ExtendWith(MockitoExtension.class)
public class ThemeServiceTest {

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private ThemeDao themeDao;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void 존재하지_않는_테마_삭제_예외발생() {
        Long notFoundId = 999L;

        assertThatThrownBy(() -> themeService.removeById(notFoundId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.THEME_NOT_FOUND.getMessage());

    }

    @Test
    void 예약이_존재하지_않는_테마_삭제_성공() {
        Long themeId = 10L;

        given(themeDao.selectById(themeId))
                .willReturn(Optional.of(new Theme(themeId, "테마", "설명", "image")));
        given(reservationDao.existsByThemeIdAndAfterDate(eq(themeId), any(LocalDate.class)))
                .willReturn(false);

        themeService.removeById(themeId);

        verify(themeDao, times(1)).deleteById(themeId);
    }

    @Test
    void 예약이_존재하는_테마_삭제_예외발생() {
        Long themeId = 10L;

        given(themeDao.selectById(themeId))
                .willReturn(Optional.of(new Theme(themeId, "테마", "설명", "image")));
        given(reservationDao.existsByThemeIdAndAfterDate(eq(themeId), any(LocalDate.class)))
                .willReturn(true);

        assertThatThrownBy(() -> themeService.removeById(themeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.CANNOT_DELETE_RESERVED_THEME.getMessage());
    }
}
