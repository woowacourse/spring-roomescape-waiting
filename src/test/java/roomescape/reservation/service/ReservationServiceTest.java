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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.payment.service.PaymentService;
import roomescape.reservation.MyReservation;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationStatus;
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

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private ReservationService reservationService;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    @Test
    void 이름으로_예약과_예약_대기를_한_번에_조회_성공() {
        String name = "초록";
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = new Reservation(1L, name, 1L, date, new ReservationTime(1L, LocalTime.of(10, 0)));
        ReservationWaiting waiting = new ReservationWaiting(2L, name, 2L, date,
                new ReservationTime(2L, LocalTime.of(11, 0)), LocalDateTime.now(), 1L);

        when(reservationDao.selectByName(name)).thenReturn(List.of(reservation));
        when(reservationWaitingDao.selectByName(name)).thenReturn(List.of(waiting));
        when(themeDao.selectById(1L)).thenReturn(Optional.of(new Theme(1L, "예약 테마", "설명", "image")));
        when(themeDao.selectById(2L)).thenReturn(Optional.of(new Theme(2L, "대기 테마", "설명", "image")));

        List<MyReservation> actual = reservationService.findAllByName(name);

        assertThat(actual).hasSize(2);
        assertThat(actual.getFirst())
                .extracting(MyReservation::getId, MyReservation::getName, MyReservation::getThemeName,
                        MyReservation::getStartAt, MyReservation::getResourceType, MyReservation::getStatus,
                        MyReservation::getWaitingNumber)
                .containsExactly(1L, name, "예약 테마", LocalTime.of(10, 0), "reservation", "예약 확정", null);
        assertThat(actual.get(1))
                .extracting(MyReservation::getId, MyReservation::getName, MyReservation::getThemeName,
                        MyReservation::getStartAt, MyReservation::getResourceType, MyReservation::getStatus,
                        MyReservation::getWaitingNumber)
                .containsExactly(2L, name, "대기 테마", LocalTime.of(11, 0), "waiting", "대기중", 1L);
        verify(reservationDao).selectByName(name);
        verify(reservationWaitingDao).selectByName(name);
        verifyNoInteractions(timeDao);
    }

    @Test
    void 이름으로_예약만_조회_성공() {
        String name = "초록";
        LocalDate date = LocalDate.now();
        List<Reservation> reservations = List.of(
                new Reservation(1L, name, 1L, date, new ReservationTime(1L, LocalTime.of(10, 0))),
                new Reservation(2L, name, 1L, date, new ReservationTime(2L, LocalTime.of(11, 0)))
        );

        when(reservationDao.selectByName(name)).thenReturn(reservations);
        when(reservationWaitingDao.selectByName(name)).thenReturn(List.of());
        when(themeDao.selectById(1L)).thenReturn(Optional.of(new Theme(1L, "테마", "설명", "image")));

        List<MyReservation> actual = reservationService.findAllByName(name);

        assertThat(actual).hasSize(2)
                .allSatisfy(myReservation -> {
                    assertThat(myReservation.getResourceType()).isEqualTo("reservation");
                    assertThat(myReservation.getWaitingNumber()).isNull();
                });
    }

    @Test
    void 이름으로_예약_대기만_조회_성공() {
        String name = "초록";
        ReservationWaiting waiting = new ReservationWaiting(1L, name, 1L, LocalDate.now().plusDays(1),
                new ReservationTime(1L, LocalTime.of(10, 0)), LocalDateTime.now(), 1L);

        when(reservationDao.selectByName(name)).thenReturn(List.of());
        when(reservationWaitingDao.selectByName(name)).thenReturn(List.of(waiting));
        when(themeDao.selectById(1L)).thenReturn(Optional.of(new Theme(1L, "은하수", "설명", "image")));

        List<MyReservation> actual = reservationService.findAllByName(name);

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().getName()).isEqualTo(name);
        assertThat(actual.getFirst().getThemeName()).isEqualTo("은하수");
        assertThat(actual.getFirst().getWaitingNumber()).isEqualTo(1L);
    }

    @Test
    void 이름으로_조회한_예약과_예약_대기가_없으면_빈_목록을_반환한다() {
        String name = "에버";
        when(reservationDao.selectByName(name)).thenReturn(List.of());
        when(reservationWaitingDao.selectByName(name)).thenReturn(List.of());

        List<MyReservation> actual = reservationService.findAllByName(name);

        assertThat(actual).isEmpty();
    }

    @Test
    void 지난_날짜및시간_예약_하는_경우_예외발생() {
        ReservationTime mockTime = new ReservationTime(17L, LocalTime.now().minusMinutes(10));
        when(themeDao.selectById(anyLong())).thenReturn(Optional.of(new Theme("name", "description", "image")));
        when(timeDao.selectById(anyLong())).thenReturn(Optional.of(mockTime));

        assertThatThrownBy(() -> reservationService.add("브라운", 1L, LocalDate.now().minusDays(1), 1L))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.CANNOT_RESERVE_PAST_DATETIME.getMessage());

        assertThatThrownBy(() -> reservationService.add("브라운", 1L, LocalDate.now(), mockTime.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.CANNOT_RESERVE_PAST_DATETIME.getMessage());
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

        given(reservationDao.selectByIdForUpdate(reservationId))
                .willReturn(Optional.of(originReservation));
        given(reservationWaitingDao.selectFirstWaitingForUpdate(originReservation))
                .willReturn(Optional.empty());

        reservationService.deleteByIdIfNameMatches(reservationId, name);

        verify(reservationDao, times(1)).deleteById(reservationId);
        verify(reservationDao, times(0)).updateNameByThemeIdAndDateAndTimeId(any(Reservation.class));
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

        given(reservationDao.selectByIdForUpdate(reservationId))
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
        given(reservationDao.selectByIdForUpdate(pastReserved)).willReturn(Optional.of(reservation));

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

        when(reservationDao.selectByIdForUpdate(id)).thenReturn(Optional.of(reservation));
        when(reservationWaitingDao.selectFirstWaitingForUpdate(reservation)).thenReturn(Optional.of(waiting));
        when(reservationDao.updateNameByThemeIdAndDateAndTimeId(any(Reservation.class)))
                .thenReturn(Optional.of(new Reservation(id, waiting.getName(), themeId, date, reservationTime)));

        reservationService.deleteByIdIfNameMatches(reservation.getId(), reservation.getName());

        ArgumentCaptor<Reservation> approvedReservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationDao, times(1)).updateNameByThemeIdAndDateAndTimeId(approvedReservationCaptor.capture());
        Reservation approvedReservation = approvedReservationCaptor.getValue();

        assertThat(approvedReservation.getId()).isEqualTo(id);
        assertThat(approvedReservation.getName()).isEqualTo(waiting.getName());
        assertThat(approvedReservation.getThemeId()).isEqualTo(themeId);
        assertThat(approvedReservation.getDate()).isEqualTo(date);
        assertThat(approvedReservation.getTime().getId()).isEqualTo(timeId);
        assertThat(approvedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        verify(reservationDao, times(0)).deleteById(id);
        verify(reservationWaitingDao, times(1)).deleteById(waiting.getId());
        verify(paymentService, times(1)).createReservationOrder(id);
    }

    @Test
    void 자동_예약_전환_대상이_없으면_예약_삭제_성공() {
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

        when(reservationDao.selectByIdForUpdate(id)).thenReturn(Optional.of(reservation));
        when(reservationWaitingDao.selectFirstWaitingForUpdate(reservation)).thenReturn(Optional.empty());

        reservationService.deleteByIdIfNameMatches(reservation.getId(), reservation.getName());

        verify(reservationDao, times(0)).updateNameByThemeIdAndDateAndTimeId(any(Reservation.class));
        verify(reservationDao, times(1)).deleteById(id);
        verify(reservationWaitingDao, times(0)).deleteById(anyLong());
        verifyNoInteractions(paymentService);
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

        when(reservationDao.selectByIdForUpdate(id)).thenReturn(Optional.of(reservation));
        when(reservationWaitingDao.selectFirstWaitingForUpdate(reservation)).thenReturn(Optional.of(waiting));
        when(reservationDao.updateNameByThemeIdAndDateAndTimeId(any(Reservation.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteByIdIfNameMatches(reservation.getId(), reservation.getName()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.RESERVATION_NOT_FOUND.getMessage());

        ArgumentCaptor<Reservation> approvedReservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationDao, times(1)).updateNameByThemeIdAndDateAndTimeId(approvedReservationCaptor.capture());
        assertThat(approvedReservationCaptor.getValue().getName()).isEqualTo(waiting.getName());
        verify(reservationDao, times(0)).deleteById(id);
        verify(reservationWaitingDao, times(0)).deleteById(waiting.getId());
        verifyNoInteractions(paymentService);
    }
}
