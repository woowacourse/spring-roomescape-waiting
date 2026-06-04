package roomescape.feature.reservation.cancel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlotReleasedHandler {

    private final WaitingPromoter waitingPromoter;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSlotReleasedEvent(SlotReleasedEvent event) {
        try {
            waitingPromoter.promoteFastestWaiting(event);
        } catch (Exception exception) {
            log.error(
                    "대기 예약 자동 승격 처리 중 예기치 못한 오류가 발생했습니다. date={}, timeId={}, themeId={}",
                    event.date(), event.timeId(), event.themeId(), exception
            );
        }
    }
}
