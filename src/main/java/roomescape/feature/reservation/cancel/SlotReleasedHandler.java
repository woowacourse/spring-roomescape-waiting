package roomescape.feature.reservation.cancel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.feature.reservation.domain.Slot;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlotReleasedHandler {

    private final WaitingPromoter waitingPromoter;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSlotReleasedEvent(SlotReleasedEvent event) {
        Slot slot = event.slot();
        try {
            waitingPromoter.promoteFastestWaiting(slot);
        } catch (Exception exception) {
            log.error(
                    "대기 예약 자동 승격 처리 중 예기치 못한 오류가 발생했습니다. date={}, timeId={}, themeId={}",
                    slot.getDate(), slot.getTimeId(), slot.getThemeId(), exception
            );
        }
    }
}
