package roomescape.waiting.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.theme.Theme;
import roomescape.theme.dao.ThemeDao;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private ThemeDao themeDao;

    @Mock
    private TimeDao timeDao;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;


    @Test
    void 중복_예약_대기_신청하는_경우_예외_발생() {
        String name = "ever";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.now().plusHours(1));

        when(themeDao.selectById(themeId))
                .thenReturn(Optional.of(new Theme(themeId, "theme", "description", "image")));
        when(timeDao.selectById(timeId))
                .thenReturn(Optional.of(reservationTime));
        when(reservationDao.notExistsByDateAndThemeIdAndTimeId(themeId, date, timeId))
                .thenReturn(false);
        when(reservationDao.existsByNameAndDateAndThemeIdAndTimeId(name, themeId, date, timeId))
                .thenReturn(false);
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

        when(reservationDao.notExistsByDateAndThemeIdAndTimeId(themeId, date, timeId))
                .thenReturn(false);
        when(reservationDao.existsByNameAndDateAndThemeIdAndTimeId(name, themeId, date, timeId))
                .thenReturn(false);
        when(reservationWaitingDao.existsByNameAndDateAndThemeIdAndTimeId(
                name, themeId, date, timeId
        )).thenReturn(false);
        when(themeDao.selectById(themeId))
                .thenReturn(Optional.of(new Theme(themeId, "theme", "description", "image")));

        when(timeDao.selectById(anyLong())).thenReturn(Optional.of(reservationTime));

        when(reservationWaitingDao.insert(any(ReservationWaiting.class)))
                .thenAnswer(invocation -> {
                    ReservationWaiting waiting = invocation.getArgument(0);
                    return new ReservationWaiting(
                            1L,
                            waiting.getName(),
                            waiting.getThemeId(),
                            waiting.getDate(),
                            waiting.getTime(),
                            waiting.getCreatedAt(),
                            100L
                    );
                });

        ReservationWaiting actual = reservationWaitingService.add(name, themeId, date, timeId);

        assertThat(actual.getWaitingNumber()).isEqualTo(100L);
    }

    @Test
    void 존재하지_않는_시간으로_예약_대기_신청하는_경우_예외_발생() {
        String name = "ever";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;

        when(themeDao.selectById(themeId))
                .thenReturn(Optional.of(new Theme(themeId, "theme", "description", "image")));

        when(timeDao.selectById(timeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.add(name, themeId, date, timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.RESERVATION_TIME_NOT_FOUND.getMessage());
    }

    @Test
    void 존재하지_않는_예약에_대기_신청하는_경우_예외_발생() {
        String name = "ever";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.now().plusHours(1));

        when(themeDao.selectById(themeId))
                .thenReturn(Optional.of(new Theme(themeId, "theme", "description", "image")));
        when(timeDao.selectById(timeId))
                .thenReturn(Optional.of(reservationTime));
        when(reservationDao.notExistsByDateAndThemeIdAndTimeId(themeId, date, timeId))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationWaitingService.add(name, themeId, date, timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.RESERVATION_NOT_EXISTS.getMessage());

        verify(reservationDao, never()).existsByNameAndDateAndThemeIdAndTimeId(anyString(), anyLong(),
                any(LocalDate.class), anyLong());
        verify(reservationWaitingDao, never()).existsByNameAndDateAndThemeIdAndTimeId(anyString(), anyLong(), any(LocalDate.class), anyLong());
    }

    @Test
    void 본인_예약에_대기_신청하는_경우_예외_발생() {
        String name = "ever";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.now().plusHours(1));

        when(themeDao.selectById(themeId))
                .thenReturn(Optional.of(new Theme(themeId, "theme", "description", "image")));
        when(timeDao.selectById(timeId))
                .thenReturn(Optional.of(reservationTime));
        when(reservationDao.notExistsByDateAndThemeIdAndTimeId(themeId, date, timeId))
                .thenReturn(false);
        when(reservationDao.existsByNameAndDateAndThemeIdAndTimeId(name, themeId, date, timeId))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationWaitingService.add(name, themeId, date, timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.DUPLICATED_RESERVATION.getMessage());

        verify(reservationWaitingDao, never()).existsByNameAndDateAndThemeIdAndTimeId(anyString(), anyLong(), any(LocalDate.class), anyLong());
    }

    @Test
    void 존재하지_않는_테마로_예약_대기_신청하는_경우_예외_발생() {
        String name = "ever";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;

        when(themeDao.selectById(themeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.add(name, themeId, date, timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.THEME_NOT_FOUND.getMessage());

        verify(timeDao, never()).selectById(anyLong());
        verify(reservationWaitingDao, never()).insert(any(ReservationWaiting.class));
    }

    @Test
    void 과거_시간으로_예약_대기_신청하는_경우_예외_발생() {
        String name = "ever";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().minusDays(1);
        Long timeId = 1L;
        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.now().minusHours(1));

        when(themeDao.selectById(themeId))
                .thenReturn(Optional.of(new Theme(themeId, "theme", "description", "image")));
        when(timeDao.selectById(timeId))
                .thenReturn(Optional.of(reservationTime));

        assertThatThrownBy(() -> reservationWaitingService.add(name, themeId, date, timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.CANNOT_CANCEL_PAST_RESERVATION_WAITING.getMessage());

        verify(reservationWaitingDao, never()).insert(any(ReservationWaiting.class));
    }

    @Test
    void 예약_대기_상세_조회_시_조회된_순번을_반환_성공() {
        Long waitingId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        LocalDateTime createdAt = LocalDateTime.now();
        ReservationWaiting reservationWaiting = new ReservationWaiting(
                waitingId, "ever", 1L, date, reservationTime, createdAt, 3L
        );

        when(reservationWaitingDao.selectById(waitingId))
                .thenReturn(Optional.of(reservationWaiting));

        ReservationWaiting actual = reservationWaitingService.getWaitingDetails(waitingId);

        assertThat(actual.getWaitingNumber()).isEqualTo(3L);
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
        ReservationWaiting reservationWaiting = new ReservationWaiting("other", 1L, LocalDate.now().plusDays(1),
                reservationTime, LocalDateTime.now());

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
        ReservationWaiting reservationWaiting = new ReservationWaiting(name, 1L, LocalDate.now().minusDays(1),
                reservationTime, LocalDateTime.now());

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
        ReservationWaiting reservationWaiting = new ReservationWaiting(name, 1L, LocalDate.now().plusDays(1),
                reservationTime, LocalDateTime.now());

        when(reservationWaitingDao.selectById(id))
                .thenReturn(Optional.of(reservationWaiting));

        reservationWaitingService.deleteByIdIfNameMatches(id, name);

        verify(reservationWaitingDao, times(1)).deleteById(id);
    }
}
