package roomescape.reservation.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import roomescape.reservation.application.service.PromotionService;
import roomescape.reservation.application.service.WaitingService;
import roomescape.reservation.event.schema.WaitingPromotedToReservation;
import roomescape.reservation.event.schema.WaitingSaved;

@Component
@RequiredArgsConstructor
public class WaitingEventListener {

    private final PromotionService promotionService;
    private final WaitingService waitingService;

    @EventListener
    public void handleWaitingSaved(WaitingSaved event) {
        promotionService.promoteFromWaiting(event.date(), event.themeId(), event.timeId());
    }

    @EventListener
    public void handleWaitingPromoted(WaitingPromotedToReservation event) {
        waitingService.deleteOldestBySlot(event.date(), event.themeId(), event.timeId());
    }
}
