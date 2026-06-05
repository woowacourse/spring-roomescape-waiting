package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.time.dao.TimeDao;

@ExtendWith(MockitoExtension.class)
class TimeServiceTest {

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private TimeDao timeDao;

    @InjectMocks
    private TimeService timeService;

    @Test
    void 아직_지나지_않은_예약이_점유중인_시간_삭제시_예외발생() {
        Long timeId = 1L;
        when(reservationDao.existsUpcomingByTimeId(anyLong(), any(LocalDate.class), any(LocalTime.class)))
                .thenReturn(true);

        assertThatThrownBy(() -> timeService.deleteById(timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.CANNOT_DELETE_RESERVED_TIME.getMessage());

        verify(timeDao, never()).deleteById(anyLong());
    }

    @Test
    void 점유중인_예약이_없으면_시간_삭제_성공() {
        Long timeId = 1L;
        when(reservationDao.existsUpcomingByTimeId(anyLong(), any(LocalDate.class), any(LocalTime.class)))
                .thenReturn(false);

        timeService.deleteById(timeId);

        verify(timeDao, times(1)).deleteById(timeId);
    }
}
