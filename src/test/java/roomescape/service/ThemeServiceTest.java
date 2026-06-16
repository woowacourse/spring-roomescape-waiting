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
import roomescape.dao.ThemeRepository;
import roomescape.service.exception.ReservationConflictException;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock private ThemeRepository themeRepository;
    @Mock private ReservationDao reservationDao;
    @InjectMocks private ThemeService themeService;

    @Test
    void delete_정상_삭제() {
        given(reservationDao.existsByThemeId(1L)).willReturn(false);

        themeService.delete(1L);

        then(themeRepository).should().deleteById(1L);
    }

    @Test
    void delete_예약에_사용중인_테마이면_예외() {
        given(reservationDao.existsByThemeId(1L)).willReturn(true);

        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("예약에 사용 중인 테마는 삭제할 수 없습니다.");
    }
}
