package roomescape.feature.reservation.cancel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.repository.ReservationRepository;

@Component
@RequiredArgsConstructor
public class ReservationCancelHandler {

    private final ReservationRepository reservationRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmFastestWaiting(ReservationCancelEvent event) {
        Reservation highestPriorityWaiting = reservationRepository.findLowestIdWaitingReservation(
                event.date(),
                event.timeId(),
                event.themeId()
        ).orElse(null);

        if (highestPriorityWaiting == null) {
            return;
        }

        Reservation confirmedWaiting = highestPriorityWaiting.confirmWaiting();
        reservationRepository.update(confirmedWaiting);
    }
}
