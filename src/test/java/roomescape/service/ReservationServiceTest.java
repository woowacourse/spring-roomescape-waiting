package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainConflictException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.BusinessException;
import roomescape.service.exception.ErrorCode;
import roomescape.service.exception.ResourceNotFoundException;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        reservationService = new ReservationService(
                reservationRepository, reservationTimeRepository, themeRepository, fixedClock
        );
    }

    @Test
    void 예약_날짜가_과거인_경우_도메인_충돌_예외가_발생한다() {
        ReservationTime newTime = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "공포방", "무서운방입니다.", "image-url");

        when(reservationTimeRepository.findById(anyLong())).thenReturn(Optional.of(newTime));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));

        assertThatThrownBy(() -> reservationService.createReservation("레서", LocalDate.of(2026, 4, 1), 1L, 1L))
                .isInstanceOf(DomainConflictException.class);

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 이미_예약된_예약_시간인_경우_예외가_발생한다() {
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "공포방", "무서운방입니다.", "image-url");
        when(reservationTimeRepository.findById(anyLong())).thenReturn(Optional.of(newTime));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));
        when(reservationRepository.findBySchedule(any(Schedule.class)))
                .thenReturn(Optional.of(new Reservation(
                        1L, "브라운", new Schedule(LocalDate.of(2026, 5, 13), newTime, theme))));

        assertThatThrownBy(() -> reservationService.createReservation("레서", LocalDate.of(2026, 5, 13), 1L, 1L))
                .isInstanceOf(BusinessConflictException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_RESERVATION);
    }

    @Test
    void 존재하지_않는_시간으로_예약을_생성하면_예외가_발생하고_예약을_저장하지_않는다() {
        when(reservationTimeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(
                "브라운", LocalDate.of(2026, 5, 10), 999L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_TIME_NOT_FOUND);

        verify(themeRepository, never()).findById(2L);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_테마로_예약을_생성하면_예외가_발생하고_예약을_저장하지_않는다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(12, 0));
        when(reservationTimeRepository.findById(anyLong())).thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(
                "브라운", LocalDate.of(2026, 5, 10), 1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.THEME_NOT_FOUND);

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_예약을_변경하면_예외가_발생한다() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.updateReservation(999L, "브라운", LocalDate.of(2026, 5, 11), 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);

        verify(reservationTimeRepository, never()).findById(anyLong());
        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @Test
    void 본인의_예약이_아닌_경우_변경하면_도메인_충돌_예외가_발생한다() {
        ReservationTime originalTime = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "공포방", "무서운방입니다.", "image-url");
        Reservation reservation = new Reservation(7L, "브라운", new Schedule(LocalDate.of(2026, 5, 10), originalTime, theme));

        when(reservationRepository.findById(7L)).thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(2L)).thenReturn(Optional.of(newTime));

        assertThatThrownBy(() -> reservationService.updateReservation(7L, "어셔", LocalDate.of(2026, 5, 11), 2L))
                .isInstanceOf(DomainConflictException.class);

        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_시간으로_예약을_변경하면_예외가_발생한다() {
        Reservation reservation = new Reservation(
                7L,
                "브라운",
                new Schedule(
                        LocalDate.of(2026, 5, 10),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "공포방", "무서운방입니다.", "image-url"))
        );
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.updateReservation(7L, "브라운", LocalDate.of(2026, 5, 11), 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_TIME_NOT_FOUND);

        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @Test
    void 이미_예약된_날짜와_시간으로_예약을_변경하면_예외가_발생한다() {
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(12, 0));
        Reservation reservation = new Reservation(
                7L,
                "브라운",
                new Schedule(
                        LocalDate.of(2026, 5, 10),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "공포방", "무서운방입니다.", "image-url"))
        );
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(2L)).thenReturn(Optional.of(newTime));
        when(reservationRepository.findBySchedule(any(Schedule.class)))
                .thenReturn(Optional.of(new Reservation(
                        8L,
                        "어셔",
                        new Schedule(
                                LocalDate.of(2026, 5, 11),
                                newTime,
                                reservation.getSchedule().getTheme())
                )));

        assertThatThrownBy(() -> reservationService.updateReservation(7L, "브라운", LocalDate.of(2026, 5, 11), 2L))
                .isInstanceOf(BusinessConflictException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_RESERVATION);

        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_예외가_발생한다() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteUserReservation(999L, "레서"))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);

        verify(reservationRepository, never()).delete(any(Reservation.class));
    }

    @Test
    void 본인의_예약이_아닌_예약을_삭제하면_도메인_충돌_예외가_발생한다() {
        Reservation reservation = new Reservation(
                7L,
                "브라운",
                new Schedule(
                        LocalDate.of(2026, 5, 10),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "공포방", "무서운방입니다.", "image-url"))
        );
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.deleteUserReservation(7L, "레서"))
                .isInstanceOf(DomainConflictException.class);

        verify(reservationRepository, never()).delete(any(Reservation.class));
    }
}
