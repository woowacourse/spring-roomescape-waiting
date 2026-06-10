package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryDao;
import roomescape.repository.ReservationTimeQueryDao;
import roomescape.repository.ReservationUpdateDao;
import roomescape.repository.ReservationWaitingQueryDao;
import roomescape.repository.ReservationWaitingUpdateDao;
import roomescape.repository.ThemeQueryDao;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationQueryDao reservationQueryingDao;

    @Mock
    private ReservationUpdateDao reservationUpdatingDao;

    @Mock
    private ReservationTimeQueryDao reservationTimeQueryingDao;

    @Mock
    private ThemeQueryDao themeQueryingDao;

    @Mock
    private ReservationWaitingQueryDao reservationWaitingQueryDao;

    @Mock
    private ReservationWaitingUpdateDao reservationWaitingUpdateDao;

    @InjectMocks
    private ReservationService reservationService;

    private static final LocalDate future = LocalDate.now().plusDays(1);
    private static final ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));
    private static final Theme theme = new Theme(1L, "테마", "설명", "http://example.com");

    @Test
    void 예약_생성_성공() {
        when(reservationTimeQueryingDao.findReservationTimeById(1L)).thenReturn(Optional.of(time));
        when(themeQueryingDao.findThemeById(1L)).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationBySlot(any())).thenReturn(Optional.empty());
        when(reservationUpdatingDao.insert(any())).thenReturn(1L);

        ReservationResponse reservationResponse = reservationService.create(new ReservationRequest("브라운", future, 1L, 1L));

        assertThat(reservationResponse.getId()).isNotNull();
        assertThat(reservationResponse.getName()).isEqualTo("브라운");
    }

    @Test
    void 과거_날짜로_예약시_예외가_발생한다() {
        when(reservationTimeQueryingDao.findReservationTimeById(1L)).thenReturn(Optional.of(time));
        when(themeQueryingDao.findThemeById(1L)).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationBySlot(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.create(new ReservationRequest("브라운", LocalDate.now().minusDays(1), 1L, 1L)))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 중복된_테마_날짜_시간으로_예약하면_예외가_발생한다() {
        Reservation existedReservation = new Reservation(1L, "네오", new ReservationSlot(future, time, theme), LocalDateTime.now());
        when(reservationTimeQueryingDao.findReservationTimeById(1L)).thenReturn(Optional.of(time));
        when(themeQueryingDao.findThemeById(1L)).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationBySlot(any())).thenReturn(Optional.of(existedReservation));

        assertThatThrownBy(() -> reservationService.create(new ReservationRequest("브라운", future, 1L, 1L)))
                .isInstanceOf(ReservationAlreadyExistException.class);
    }

    @Test
    void 존재하지_않는_시간으로_예약시_예외가_발생한다() {
        when(reservationTimeQueryingDao.findReservationTimeById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.create(new ReservationRequest("브라운", future, 999L, 1L)))
                .isInstanceOf(ReservationTimeNotFoundException.class);
    }

    @Test
    void 존재하지_않는_테마로_예약시_예외가_발생한다() {
        when(reservationTimeQueryingDao.findReservationTimeById(1L)).thenReturn(Optional.of(time));
        when(themeQueryingDao.findThemeById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.create(new ReservationRequest("브라운", future, 1L, 999L)))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void 전체_예약_조회() {
        Reservation reservation1 = new Reservation(1L, "브라운", new ReservationSlot(future, time, theme), LocalDateTime.now());
        Reservation reservation2 = new Reservation(2L, "네오", new ReservationSlot(future.plusDays(1), time, theme), LocalDateTime.now());
        when(reservationQueryingDao.findAllReservations()).thenReturn(List.of(reservation1, reservation2));

        List<ReservationResponse> result = reservationService.readAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void 예약_날짜_및_시간_변경() {
        ReservationTime newTime = new ReservationTime(2L, LocalTime.parse("11:00"));
        Reservation existedReservation = new Reservation(1L, "브라운", new ReservationSlot(future, time, theme), LocalDateTime.now());
        when(reservationQueryingDao.findReservationById(1L)).thenReturn(Optional.of(existedReservation));
        when(reservationTimeQueryingDao.findReservationTimeById(2L)).thenReturn(Optional.of(newTime));
        when(reservationQueryingDao.findReservationBySlot(any())).thenReturn(Optional.empty());

        ReservationResponse updated = reservationService.update(1L, new ReservationRequest("브라운", future.plusDays(1), 2L, 1L));

        assertThat(updated.getDate()).isEqualTo(future.plusDays(1));
    }

    @Test
    void 과거_날짜로_변경시_예외가_발생한다() {
        Reservation existedReservation = new Reservation(1L, "브라운", new ReservationSlot(future, time, theme), LocalDateTime.now());
        ReservationTime newTime = new ReservationTime(2L, LocalTime.parse("11:00"));
        when(reservationQueryingDao.findReservationById(1L)).thenReturn(Optional.of(existedReservation));
        when(reservationTimeQueryingDao.findReservationTimeById(2L)).thenReturn(Optional.of(newTime));

        assertThatThrownBy(() -> reservationService.update(1L, new ReservationRequest("브라운", LocalDate.now().minusDays(1), 2L, 1L)))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 이미_예약된_시간으로_변경시_예외가_발생한다() {
        ReservationTime newTime = new ReservationTime(2L, LocalTime.parse("11:00"));
        Reservation existedReservation = new Reservation(1L, "브라운", new ReservationSlot(future, time, theme), LocalDateTime.now());
        Reservation duplicatedReservation = new Reservation(2L, "네오", new ReservationSlot(future, newTime, theme), LocalDateTime.now());
        when(reservationQueryingDao.findReservationById(1L)).thenReturn(Optional.of(existedReservation));
        when(reservationTimeQueryingDao.findReservationTimeById(2L)).thenReturn(Optional.of(newTime));
        when(reservationQueryingDao.findReservationBySlot(any())).thenReturn(Optional.of(duplicatedReservation));

        assertThatThrownBy(() -> reservationService.update(1L, new ReservationRequest("브라운", future, 2L, 1L)))
                .isInstanceOf(ReservationAlreadyExistException.class);
    }

    @Test
    void 존재하지_않는_예약_변경시_예외가_발생한다() {
        when(reservationQueryingDao.findReservationById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.update(999L, new ReservationRequest("브라운", future, 1L, 1L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 대기_없는_예약_취소_시_예약만_삭제된다() {
        Reservation reservation = new Reservation(1L, "브라운", new ReservationSlot(future, time, theme), LocalDateTime.now());
        when(reservationQueryingDao.findReservationById(1L)).thenReturn(Optional.of(reservation));
        when(reservationWaitingQueryDao.findFirstWaitingBySlot(any())).thenReturn(Optional.empty());

        reservationService.delete(1L);

        verify(reservationUpdatingDao).delete(1L);
        verify(reservationUpdatingDao, never()).insert(any());
    }

    @Test
    void 대기_있는_예약_취소_시_대기_1번이_예약으로_전환된다() {
        Reservation reservation = new Reservation(1L, "브라운", new ReservationSlot(future, time, theme), LocalDateTime.now());
        ReservationWaiting waiting = new ReservationWaiting(1L, "네오", new ReservationSlot(future, time, theme), LocalDateTime.now());
        when(reservationQueryingDao.findReservationById(1L)).thenReturn(Optional.of(reservation));
        when(reservationWaitingQueryDao.findFirstWaitingBySlot(any())).thenReturn(Optional.of(waiting));

        reservationService.delete(1L);

        verify(reservationUpdatingDao).insert(any());
        verify(reservationWaitingUpdateDao).delete(waiting.getId());
    }
}