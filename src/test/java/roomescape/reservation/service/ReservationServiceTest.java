package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.reservation.MyReservation;
import roomescape.reservation.Reservation;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.dto.ReservationChangeRequest;
import roomescape.theme.Theme;
import roomescape.theme.dao.ThemeDao;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;
import roomescape.waiting.service.ReservationWaitingService;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private ThemeDao themeDao;

    @Mock
    private TimeDao timeDao;

    @Mock
    private ReservationWaitingDao reservationWaitingDao;

    @InjectMocks
    private ReservationService reservationService;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    @Test
    void 이름으로_예약과_예약_대기를_한_번에_조회한다() {
        String name = "초록";
        LocalDate date = LocalDate.now().plusDays(1);
        List<MyReservation> expected = List.of(
                new MyReservation(1L, name, "예약 테마", date, LocalTime.of(10, 0),
                        "reservation", "예약 확정", null),
                new MyReservation(2L, name, "대기 테마", date, LocalTime.of(11, 0),
                        "waiting", "대기중", 1L)
        );

        when(reservationDao.selectAllCombinedByName(name)).thenReturn(expected);

        List<MyReservation> actual = reservationService.findAllByName(name);

        assertThat(actual).isEqualTo(expected);
        verify(reservationDao).selectAllCombinedByName(name);
        verifyNoInteractions(themeDao, timeDao);
    }

    @Test
    void 이름으로_예약만_조회_성공() {
        String name = "초록";
        List<MyReservation> expected = List.of(
                new MyReservation(1L, name, "테마", LocalDate.now(), LocalTime.of(10, 0),
                        "reservation", "예약 확정", null),
                new MyReservation(2L, name, "테마", LocalDate.now(), LocalTime.of(11, 0),
                        "reservation", "예약 확정", null)
        );

        when(reservationDao.selectAllCombinedByName(name)).thenReturn(expected);

        List<MyReservation> actual = reservationService.findAllByName(name);

        assertThat(actual).hasSize(2)
                .allSatisfy(myReservation -> {
                    assertThat(myReservation.resourceType()).isEqualTo("reservation");
                    assertThat(myReservation.waitingNumber()).isNull();
                });
    }

    @Test
    void 이름으로_예약_대기만_조회_성공() {
        String name = "초록";
        MyReservation waiting = new MyReservation(1L, name, "은하수", LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), "waiting", "대기중", 1L);

        when(reservationDao.selectAllCombinedByName(name)).thenReturn(List.of(waiting));

        List<MyReservation> actual = reservationService.findAllByName(name);

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().name()).isEqualTo(name);
        assertThat(actual.getFirst().themeName()).isEqualTo("은하수");
        assertThat(actual.getFirst().waitingNumber()).isEqualTo(1L);
    }

    @Test
    void 이름으로_조회한_예약과_예약_대기가_없으면_빈_목록을_반환한다() {
        String name = "에버";
        when(reservationDao.selectAllCombinedByName(name)).thenReturn(List.of());

        List<MyReservation> actual = reservationService.findAllByName(name);

        assertThat(actual).isEmpty();
    }

    @Test
    void 지난_날짜및시간_예약_하는_경우_예외발생() {
        ReservationTime mockTime = new ReservationTime(17L, LocalTime.now().minusMinutes(10));
        when(timeDao.selectById(anyLong())).thenReturn(Optional.of(mockTime));

        assertThatThrownBy(() -> reservationService.add("브라운", 1L, LocalDate.now().minusDays(1), 1L))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.PAST_RESERVATION.getMessage());

        assertThatThrownBy(() -> reservationService.add("브라운", 1L, LocalDate.now(), mockTime.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.PAST_RESERVATION.getMessage());
    }

    @Test
    void 이미_예약이_존재하는_경우_예외발생() {
        Long themeId = 1L;
        ReservationTime mockTime = new ReservationTime(1L, LocalTime.parse("10:00"));
        LocalDate date = LocalDate.now().plusDays(1);
        when(timeDao.selectById(anyLong())).thenReturn(Optional.of(mockTime));
        when(themeDao.selectById(themeId))
                .thenReturn(Optional.of(new Theme(themeId, "테마", "설명", "image")));

        List<Reservation> reservations = new ArrayList<>();
        Reservation reservation = new Reservation("초록", themeId, date, mockTime);
        reservations.add(reservation);
        when(reservationDao.selectByThemeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(reservations);

        assertThatThrownBy(() -> reservationService.add("브라운", themeId, date, mockTime.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.RESERVATION_ALREADY_EXISTS.getMessage());
    }

    @Test
    void 본인_예약_변경_성공() {
        Long reservationId = 1L;
        String name = "로치";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 2L;
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(12, 0));

        Reservation originReservation = new Reservation(
                reservationId,
                name,
                themeId,
                LocalDate.now().plusDays(2),
                new ReservationTime(3L, LocalTime.of(10, 0))
        );

        given(reservationDao.selectById(reservationId))
                .willReturn(Optional.of(originReservation));
        given(timeDao.selectById(timeId))
                .willReturn(Optional.of(time));
        given(themeDao.selectById(themeId))
                .willReturn(Optional.of(new Theme(themeId, "테마", "설명", "image")));
        given(reservationDao.selectByThemeIdAndDate(themeId, date))
                .willReturn(List.of());

        Reservation changedReservation = new Reservation(reservationId, name, themeId, date, time);

        given(reservationDao.updateDateTimeById(reservationId, date, timeId))
                .willReturn(Optional.of(changedReservation));

        Reservation actual = reservationService.modifyDateTimeByName(reservationId, name, themeId, date, timeId);

        assertThat(actual.getId()).isEqualTo(reservationId);
        assertThat(actual.getName()).isEqualTo(name);
        assertThat(actual.getDate()).isEqualTo(date);
        assertThat(actual.getTime().getId()).isEqualTo(timeId);
    }

    @Test
    void 다른_사람의_예약은_변경_예외발생() {
        Long reservationId = 1L;
        Reservation reservation = new Reservation(
                reservationId,
                "로치",
                1L,
                LocalDate.now().plusDays(1),
                new ReservationTime(3L, LocalTime.of(12, 0))
        );

        ReservationChangeRequest request = new ReservationChangeRequest(
                "브라운",
                1L,
                LocalDate.now().plusDays(2),
                2L
        );

        given(reservationDao.selectById(reservationId))
                .willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.modifyDateTimeByName(
                reservationId,
                request.name(),
                request.themeId(),
                request.date(),
                request.timeId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.CANNOT_MODIFY_OTHER_RESERVATION.getMessage());
    }

    @Test
    void 존재하지_않는_예약은_변경_예외발생() {
        Long notFoundId = 999L;

        ReservationChangeRequest request = new ReservationChangeRequest(
                "로치",
                1L,
                LocalDate.now().plusDays(1),
                2L
        );

        assertThatThrownBy(() -> reservationService.modifyDateTimeByName(
                notFoundId,
                request.name(),
                request.themeId(),
                request.date(),
                request.timeId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    void 본인_예약_취소_성공() {
        Long reservationId = 1L;
        String name = "로치";
        Long themeId = 1L;

        Reservation originReservation = new Reservation(
                reservationId,
                name,
                themeId,
                LocalDate.now().plusDays(1),
                new ReservationTime(3L, LocalTime.of(10, 0))
        );

        given(reservationDao.selectById(reservationId))
                .willReturn(Optional.of(originReservation));

        reservationService.deleteByIdIfNameMatches(reservationId, name);

        verify(reservationDao, times(1)).deleteById(reservationId);
    }

    @Test
    void 다른_사람의_예약은_취소_예외발생() {
        Long reservationId = 1L;
        Reservation reservation = new Reservation(
                reservationId,
                "로치",
                1L,
                LocalDate.now().plusDays(1),
                new ReservationTime(3L, LocalTime.of(12, 0))
        );

        ReservationChangeRequest request = new ReservationChangeRequest(
                "브라운",
                1L,
                LocalDate.now().plusDays(2),
                2L
        );

        given(reservationDao.selectById(reservationId))
                .willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.deleteByIdIfNameMatches(reservationId, request.name()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.CANNOT_DELETE_OTHER_RESERVATION.getMessage());
    }

    @Test
    void 존재하지_않는_예약은_취소_예외발생() {
        Long notFoundId = 999L;

        ReservationChangeRequest request = new ReservationChangeRequest(
                "로치",
                1L,
                LocalDate.now().plusDays(1),
                2L
        );

        assertThatThrownBy(() -> reservationService.deleteByIdIfNameMatches(notFoundId, request.name()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    void 지난_예약은_취소_예외발생() {
        Long pastReserved = 1L;

        ReservationChangeRequest request = new ReservationChangeRequest(
                "로치",
                1L,
                LocalDate.now().minusDays(1),
                2L
        );
        Reservation reservation = new Reservation(
                pastReserved,
                "로치",
                1L,
                LocalDate.now().minusDays(1),
                new ReservationTime(2L, LocalTime.of(11, 0))
        );
        given(reservationDao.selectById(pastReserved)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.deleteByIdIfNameMatches(pastReserved, request.name()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.CANNOT_DELETE_PAST_RESERVATION.getMessage());
    }

    @Test
    void 예약_삭제_시_자동_예약_전환_성공() {
        Long id = 1L;
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.now().plusHours(1));
        Long waitingNumber = 1L;

        Reservation reservation = new Reservation(
                id,
                "워넬",
                themeId,
                date,
                reservationTime
        );

        ReservationWaiting waiting = new ReservationWaiting(
                id,
                "로치",
                themeId,
                date,
                reservationTime,
                LocalDateTime.now(),
                waitingNumber
        );

        when(reservationDao.selectById(id)).thenReturn(Optional.of(reservation));
        when(reservationWaitingDao.existsByThemeIdAndDateAndTimeId(themeId, date, timeId)).thenReturn(true);
        when(reservationWaitingDao.selectFirstWaitingByThemeIdAndDateAndTimeId(themeId, date, timeId)).thenReturn(waiting);
        when(reservationDao.updateNameByThemeIdAndDateAndTimeId(id, waiting.getName(), themeId, date, timeId))
                .thenReturn(Optional.of(new Reservation(id, waiting.getName(), themeId, date, reservationTime)));

        reservationService.deleteByIdIfNameMatches(reservation.getId(), reservation.getName());

        verify(reservationDao, times(1))
                .updateNameByThemeIdAndDateAndTimeId(id, waiting.getName(), themeId, date, timeId);
        verify(reservationDao, times(0)).deleteById(id);
        verify(reservationWaitingDao, times(1)).deleteById(waiting.getId());
    }

    @Test
    void 자동_예약_전환_중_첫번째_대기_조회_실패_예외발생() {
        Long id = 1L;
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.now().plusHours(1));

        Reservation reservation = new Reservation(
                id,
                "워넬",
                themeId,
                date,
                reservationTime
        );

        when(reservationDao.selectById(id)).thenReturn(Optional.of(reservation));
        when(reservationWaitingDao.existsByThemeIdAndDateAndTimeId(themeId, date, timeId)).thenReturn(true);
        when(reservationWaitingDao.selectFirstWaitingByThemeIdAndDateAndTimeId(themeId, date, timeId))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> reservationService.deleteByIdIfNameMatches(reservation.getId(), reservation.getName()))
                .isInstanceOf(EmptyResultDataAccessException.class);

        verify(reservationDao, times(0)).updateNameByThemeIdAndDateAndTimeId(anyLong(), any(), anyLong(), any(), anyLong());
        verify(reservationDao, times(0)).deleteById(id);
        verify(reservationWaitingDao, times(0)).deleteById(anyLong());
    }

    @Test
    void 자동_예약_전환_중_예약_갱신_실패_예외발생() {
        Long id = 1L;
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.now().plusHours(1));
        Long waitingNumber = 1L;

        Reservation reservation = new Reservation(
                id,
                "워넬",
                themeId,
                date,
                reservationTime
        );

        ReservationWaiting waiting = new ReservationWaiting(
                id,
                "로치",
                themeId,
                date,
                reservationTime,
                LocalDateTime.now(),
                waitingNumber
        );

        when(reservationDao.selectById(id)).thenReturn(Optional.of(reservation));
        when(reservationWaitingDao.existsByThemeIdAndDateAndTimeId(themeId, date, timeId)).thenReturn(true);
        when(reservationWaitingDao.selectFirstWaitingByThemeIdAndDateAndTimeId(themeId, date, timeId)).thenReturn(waiting);
        when(reservationDao.updateNameByThemeIdAndDateAndTimeId(id, waiting.getName(), themeId, date, timeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteByIdIfNameMatches(reservation.getId(), reservation.getName()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.RESERVATION_NOT_FOUND.getMessage());

        verify(reservationDao, times(1))
                .updateNameByThemeIdAndDateAndTimeId(id, waiting.getName(), themeId, date, timeId);
        verify(reservationDao, times(0)).deleteById(id);
        verify(reservationWaitingDao, times(0)).deleteById(waiting.getId());
    }
}
