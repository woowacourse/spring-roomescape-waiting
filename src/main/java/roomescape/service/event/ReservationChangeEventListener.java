package roomescape.service.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.service.processor.WaitingPromotionProcessor;

@Component
public class ReservationChangeEventListener {
    private final WaitingPromotionProcessor promotionProcessor;

    public ReservationChangeEventListener(WaitingPromotionProcessor promotionProcessor) {
        this.promotionProcessor = promotionProcessor;
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void promoteWaitingUser(ReservationChangeEvent event) {
        promotionProcessor.promoteWaiting(event.eventSlot());
    }
}
