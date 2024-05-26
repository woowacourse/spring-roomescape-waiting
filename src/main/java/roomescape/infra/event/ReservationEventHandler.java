package roomescape.infra.event;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;


@RequiredArgsConstructor
public class ReservationEventHandler {
    private final TaskScheduler taskScheduler;
    private final ReservationRepository reservationRepository;

    @EventListener
    public void handleReservationCancelledEvent(ReservationCancelEvent event) {
        taskScheduler.schedule(
                () -> updateNextWaitingReservation(event.getReservation()),
                Instant.now().plus(1, ChronoUnit.MINUTES)
        );
    }

    private void updateNextWaitingReservation(Reservation reservation) {
        reservationRepository.findNextWaitingReservation(reservation.getDetail())
                .ifPresent(waiting -> {
                    waiting.approve();
                    reservationRepository.save(waiting);
                });
    }
}
