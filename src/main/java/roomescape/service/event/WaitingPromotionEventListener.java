package roomescape.service.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.service.WaitingPromotionService;

@Component
public class WaitingPromotionEventListener {

    private final WaitingPromotionService waitingPromotionService;

    public WaitingPromotionEventListener(WaitingPromotionService waitingPromotionService) {
        this.waitingPromotionService = waitingPromotionService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void promote(ReservationCanceledEvent event) {
        waitingPromotionService.promoteFirstWaiting(event.slotId());
    }
}
