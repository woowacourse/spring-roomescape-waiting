package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.reservation.Reservation;
import roomescape.reservation.dao.ReservationDao;
import roomescape.time.ReservationTime;
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
    void 이후날짜에_예약이_존재하는_시간_삭제_예외발생() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("초록", 1L, LocalDate.now().plusDays(1), time));
        when(reservationDao.selectByTimeId(anyLong())).thenReturn(reservations);

        assertThatThrownBy(() -> timeService.deleteById(time.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.CANNOT_DELETE_RESERVED_TIME.getMessage());
    }

    @Test
    void 이전날짜에_예약이_존재하는_시간_삭제_성공() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("초록", 1L, LocalDate.now().minusDays(1), time));
        when(reservationDao.selectByTimeId(anyLong())).thenReturn(reservations);

        timeService.deleteById(time.getId());

        verify(timeDao, times(1)).deleteById(time.getId());
    }

    @Test
    void 당일_이후시간에_예약이_존재하는_시간_삭제_예외발생() {
        ReservationTime time = new ReservationTime(22L, LocalTime.now().plusMinutes(3));

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("초록", 1L, LocalDate.now(), time));
        when(reservationDao.selectByTimeId(anyLong())).thenReturn(reservations);

        assertThatThrownBy(() -> timeService.deleteById(time.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.CANNOT_DELETE_RESERVED_TIME.getMessage());
    }

    @Test
    void 당일_이전시간에_예약이_존재하는_시간_삭제_성공() {
        ReservationTime time = new ReservationTime(22L, LocalTime.now().minusMinutes(3));

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("초록", 1L, LocalDate.now(), time));
        when(reservationDao.selectByTimeId(anyLong())).thenReturn(reservations);

        timeService.deleteById(time.getId());

        verify(timeDao, times(1)).deleteById(time.getId());
    }
}