package roomescape.reservation.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import roomescape.reservation.application.service.PromotionService;
import roomescape.reservation.event.schema.ReservationCancelRequested;

@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final PromotionService promotionService;

    @EventListener
    public void handleReservationCancelRequested(ReservationCancelRequested event) {
        promotionService.promoteFromWaiting(event.date(), event.themeId(), event.timeId());
    }
}
