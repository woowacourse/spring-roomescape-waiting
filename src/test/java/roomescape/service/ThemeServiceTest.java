package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ThemeNotFoundException;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock private ThemeDao themeDao;
    @Mock private ReservationDao reservationDao;
    @InjectMocks private ThemeService themeService;

    @Test
    void delete_정상_삭제() {
        given(reservationDao.existsByThemeId(1L)).willReturn(false);
        given(themeDao.delete(1L)).willReturn(1);

        themeService.delete(1L);

        then(themeDao).should().delete(1L);
    }

    @Test
    void delete_예약_또는_대기에_사용중인_테마이면_예외() {
        given(reservationDao.existsByThemeId(1L)).willReturn(true);

        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("예약 또는 대기에 사용 중인 테마는 삭제할 수 없습니다.");
    }

    @Test
    void delete_존재하지_않는_테마이면_예외() {
        given(reservationDao.existsByThemeId(999L)).willReturn(false);
        given(themeDao.delete(999L)).willReturn(0);

        assertThatThrownBy(() -> themeService.delete(999L))
                .isInstanceOf(ThemeNotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }
}
