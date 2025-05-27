package roomescape.reservation.application.event;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.reservation.application.PromoteService;

@Component
@AllArgsConstructor
public class ReservationEventHandler {
    private final PromoteService promoteService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleteEvent(ReservationDeletedEvent event) {
        promoteService.promoteWaiting(event.reservation());
    }
}
