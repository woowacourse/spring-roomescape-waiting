package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.exception.DomainConflictException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.WaitingResult;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.ResourceNotFoundException;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private WaitingService waitingService;

    private ReservationTime time;
    private Theme theme;
    private final LocalDate futureDate = LocalDate.of(2026, 5, 10);

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        waitingService = new WaitingService(
                reservationTimeRepository, themeRepository, waitingRepository, reservationRepository, fixedClock
        );
        time = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "공포방", "무서운방입니다.", "image-url");
    }

    @Test
    void 예약_대기를_생성하고_대기_순번을_반환한다() {
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(waitingRepository.existsByScheduleAndName(any(), anyLong(), anyLong(), any())).thenReturn(false);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, time, theme);
        when(reservationRepository.findBySchedule(futureDate, 1L, 1L)).thenReturn(Optional.of(reservation));
        Waiting saved = new Waiting(1L, "레서", futureDate, time, theme);
        when(waitingRepository.save(any())).thenReturn(saved);
        when(waitingRepository.countByThemeIdAndDateAndTimeIdAndIdLessThanEqual(1L, theme, futureDate, time))
                .thenReturn(2L);

        WaitingResult result = waitingService.createWaiting("레서", futureDate, 1L, 1L);

        assertThat(result.waiting().getId()).isEqualTo(1L);
        assertThat(result.waiting().getName()).isEqualTo("레서");
        assertThat(result.order()).isEqualTo(2L);
        verify(waitingRepository).save(any());
    }

    @Test
    void 존재하지_않는_시간_id로_예약_대기를_생성하면_예외가_발생한다() {
        when(reservationTimeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.createWaiting("레서", futureDate, 999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(waitingRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_테마_id로_예약_대기를_생성하면_예외가_발생한다() {
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.createWaiting("레서", futureDate, 1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(waitingRepository, never()).save(any());
    }

    @Test
    void 이미_지난_날짜로_예약_대기를_생성하면_예외가_발생한다() {
        LocalDate pastDate = LocalDate.of(2026, 4, 1);
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));

        when(reservationRepository.findBySchedule(pastDate, 1L, 1L))
                .thenReturn(Optional.of(new Reservation(1L, "브라운", pastDate, time, theme)));

        assertThatThrownBy(() -> waitingService.createWaiting("레서", pastDate, 1L, 1L))
                .isInstanceOf(DomainConflictException.class);
        verify(waitingRepository, never()).save(any());
    }

    @Test
    void 동일한_일정에_이미_대기_중이면_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, "브라운", futureDate, time, theme);
        when(reservationRepository.findBySchedule(any(), anyLong(), anyLong()))
                .thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(waitingRepository.existsByScheduleAndName(any(), anyLong(), anyLong(), any())).thenReturn(true);

        assertThatThrownBy(() -> waitingService.createWaiting("레서", futureDate, 1L, 1L))
                .isInstanceOf(BusinessConflictException.class);
        verify(waitingRepository, never()).save(any());
    }

    @Test
    void 동일한_일정에_본인의_예약이_이미_있으면_예외가_발생한다() {
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(waitingRepository.existsByScheduleAndName(any(), anyLong(), anyLong(), any())).thenReturn(false);
        when(reservationRepository.findBySchedule(futureDate, 1L, 1L))
                .thenReturn(Optional.of(new Reservation(1L, "레서", futureDate, time, theme)));

        assertThatThrownBy(() -> waitingService.createWaiting("레서", futureDate, 1L, 1L))
                .isInstanceOf(BusinessConflictException.class);
        verify(waitingRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_예약_대기_id인_경우_예외가_발생한다() {
        when(waitingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.deleteWaiting(999L, "레서"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 예약_대기의_소유자가_아닌_경우_예외가_발생한다() {
        when(waitingRepository.findById(anyLong())).thenReturn(Optional.of(new Waiting(
                1L, "레서", futureDate, time, theme)));

        assertThatThrownBy(() -> waitingService.deleteWaiting(1L, "밍구"))
                .isInstanceOf(DomainConflictException.class);
    }

    @Test
    void 예약_대기의_소유자인_경우_삭제한다() {
        Waiting waiting = new Waiting(1L, "레서", futureDate, time, theme);
        when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));

        waitingService.deleteWaiting(1L, "레서");

        verify(waitingRepository).delete(waiting);
    }
}
