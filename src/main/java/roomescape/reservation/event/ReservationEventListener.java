package roomescape.reservation.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.reservation.application.service.PromotionService;
import roomescape.reservation.domain.PromotionSource;
import roomescape.reservation.event.schema.PromotionFailed;
import roomescape.reservation.event.schema.ReservationCancelRequested;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final PromotionService promotionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationCancelRequested(ReservationCancelRequested event) {
        promotionService.promoteFromWaiting(event.date(), event.themeId(), event.timeId(), PromotionSource.CANCELLATION);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePromotionFailed(PromotionFailed event) {
        if (event.retryCount() >= 3) {
            log.error("대기 승격 최종 실패 - date: {}, themeId: {}, timeId: {}",
                    event.date(),
                    event.themeId(),
                    event.timeId());
            return;
        }
        promotionService.promoteFromWaiting(event.date(), event.themeId(), event.timeId(), event.retryCount(), event.source());
    }
}
