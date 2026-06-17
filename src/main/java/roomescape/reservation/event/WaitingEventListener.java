package roomescape.reservation.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.reservation.application.service.PromotionService;
import roomescape.reservation.application.service.WaitingCommandService;
import roomescape.reservation.domain.PromotionSource;
import roomescape.reservation.event.schema.WaitingPromotedToReservation;
import roomescape.reservation.event.schema.WaitingSaved;

@Component
@RequiredArgsConstructor
public class WaitingEventListener {

    private final PromotionService promotionService;
    private final WaitingCommandService waitingCommandService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWaitingSaved(WaitingSaved event) {
        promotionService.promoteFromWaiting(event.date(), event.themeId(), event.timeId(), PromotionSource.DIRECT);
    }

    @EventListener
    public void handleWaitingPromoted(WaitingPromotedToReservation event) {
        waitingCommandService.deleteOldestBySlot(event.date(), event.themeId(), event.timeId());
    }
}
