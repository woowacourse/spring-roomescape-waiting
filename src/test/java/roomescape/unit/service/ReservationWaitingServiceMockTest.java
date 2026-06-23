package roomescape.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.domain.WaitingWithOrder;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.service.ReservationWaitingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceMockTest {

    @Mock
    private ReservationWaitingRepository reservationWaitingRepository;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    @Test
    void addWaiting은_저장소에_위임하고_저장된_대기를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(
                1L,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        Reservation reservation = new Reservation(
                1L,
                "티뉴",
                LocalDate.of(2026, 8, 5),
                time,
                theme
        );
        ReservationWaiting toSave = new ReservationWaiting("민욱", LocalDateTime.of(2026, 8, 1, 10, 0), reservation);
        WaitingWithOrder saved = new WaitingWithOrder(
                new ReservationWaiting(
                        1L,
                        "민욱",
                        toSave.getCreatedAt(),
                        reservation
                ), 1);
        given(reservationWaitingRepository.save(toSave)).willReturn(saved);

        assertThat(reservationWaitingService.addWaiting(toSave)).isEqualTo(saved);
    }

    @Test
    void findById는_존재하지_않으면_NotFoundException을_던진다() {
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.findById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById는_존재하면_대기를_반환한다() {
        ReservationWaiting waiting = waitingOwnedBy(1L, "민욱");
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        assertThat(reservationWaitingService.findById(1L)).isEqualTo(waiting);
    }

    @Test
    void cancelMyReservationWaiting은_본인_대기면_삭제를_위임한다() {
        ReservationWaiting waiting = waitingOwnedBy(1L, "민욱");
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        reservationWaitingService.cancelMyReservationWaiting(1L, "민욱");

        verify(reservationWaitingRepository).deleteById(1L);
    }

    @Test
    void cancelMyReservationWaiting은_본인_대기가_아니면_UnauthorizedException을_던진다() {
        ReservationWaiting waiting = waitingOwnedBy(1L, "민욱");
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        assertThatThrownBy(() -> reservationWaitingService.cancelMyReservationWaiting(1L, "브라운"))
                .isInstanceOf(UnauthorizedException.class);
        verify(reservationWaitingRepository, never()).deleteById(anyLong());
    }

    @Test
    void cancelMyReservationWaiting은_지난_슬롯이면_BusinessRuleViolationException을_던진다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(
                1L,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        Reservation pastReservation = new Reservation(
                1L,
                "티뉴",
                LocalDate.of(2020, 1, 1),
                time,
                theme
        );
        ReservationWaiting waiting = new ReservationWaiting(
                1L,
                "민욱",
                LocalDateTime.of(2020, 1, 1, 9, 0),
                pastReservation
        );
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        assertThatThrownBy(() -> reservationWaitingService.cancelMyReservationWaiting(1L, "민욱"))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(reservationWaitingRepository, never()).deleteById(anyLong());
    }

    private ReservationWaiting waitingOwnedBy(Long id, String name) {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(
                1L,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        Reservation reservation = new Reservation(
                1L,
                "티뉴",
                LocalDate.of(2026, 8, 5),
                time,
                theme
        );
        return new ReservationWaiting(
                id,
                name,
                LocalDateTime.of(2026, 8, 1, 10, 0),
                reservation
        );
    }
}
