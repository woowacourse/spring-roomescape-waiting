package roomescape.waiting.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationWaitingServiceTest {

    @Mock
    private ReservationWaitingDao reservationWaitingDao;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    @Mock
    private TimeDao timeDao;

    @Test
    void 중복_예약_대기_신청하는_경우_예외_발생() {
        String name = "ever";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;

        when(reservationWaitingDao.existsByNameAndDateAndThemeIdAndTimeId(anyString(), anyLong(), any(LocalDate.class), anyLong())).thenReturn(true);

        assertThatThrownBy(() ->reservationWaitingService.add(name, themeId, date, timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.DUPLICATED_RESERVATION_WAITING.getMessage());
    }

    @Test
    void 예약_대기_성공() {
        String name = "ever";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.now().plusHours(1));

        when(reservationWaitingDao.existsByNameAndDateAndThemeIdAndTimeId(
                name, themeId, date, timeId
        )).thenReturn(false);

        when(timeDao.selectById(anyLong())).thenReturn(Optional.of(reservationTime));

        when(reservationWaitingDao.findNextWaitingNumber(themeId, date, timeId))
                .thenReturn(100L);

        when(reservationWaitingDao.insert(any(ReservationWaiting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationWaiting actual = reservationWaitingService.add(name, themeId, date, timeId);

        assertThat(actual.getWaitingNumber()).isEqualTo(100L);
    }

    @Test
    void 존재하지_않는_시간으로_예약_대기_신청하는_경우_예외_발생() {
        String name = "ever";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;

        when(reservationWaitingDao.existsByNameAndDateAndThemeIdAndTimeId(
                name, themeId, date, timeId
        )).thenReturn(false);

        when(timeDao.selectById(timeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.add(name, themeId, date, timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.RESERVATION_TIME_NOT_FOUND.getMessage());
    }

    @Test
    void 존재하지_않는_예약_대기_삭제_시_예외_발생() {
        Long id = 1L;
        String name = "ever";

        when(reservationWaitingDao.selectById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.deleteByIdIfNameMatches(id, name))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.RESERVATION_WAITING_NOT_FOUND.getMessage());
    }

    @Test
    void 이름이_다른_경우_삭제_시_예외_발생() {
        Long id = 1L;
        String name = "ever";
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        ReservationWaiting reservationWaiting = new ReservationWaiting("other", 1L, LocalDate.now().plusDays(1), reservationTime, 1L);

        when(reservationWaitingDao.selectById(id))
                .thenReturn(Optional.of(reservationWaiting));

        assertThatThrownBy(() -> reservationWaitingService.deleteByIdIfNameMatches(id, name))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.FORBIDDEN_RESERVATION_WAITING_ACCESS.getMessage());
    }

    @Test
    void 과거_예약_대기_취소_시_예외_발생() {
        Long id = 1L;
        String name = "ever";
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().minusHours(1));
        ReservationWaiting reservationWaiting = new ReservationWaiting(name, 1L, LocalDate.now().minusDays(1), reservationTime, 1L);

        when(reservationWaitingDao.selectById(id))
                .thenReturn(Optional.of(reservationWaiting));

        assertThatThrownBy(() -> reservationWaitingService.deleteByIdIfNameMatches(id, name))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.CANNOT_CANCEL_PAST_RESERVATION_WAITING.getMessage());
    }

    @Test
    void 예약_대기_삭제_성공() {
        Long id = 1L;
        String name = "ever";
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        ReservationWaiting reservationWaiting = new ReservationWaiting(name, 1L, LocalDate.now().plusDays(1), reservationTime, 1L);

        when(reservationWaitingDao.selectById(id))
                .thenReturn(Optional.of(reservationWaiting));

        reservationWaitingService.deleteByIdIfNameMatches(id, name);

        verify(reservationWaitingDao, times(1)).deleteById(id);
    }
}
