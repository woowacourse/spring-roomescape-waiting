package roomescape.service.scheduler;

import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.dao.WaitingDao;
import roomescape.domain.slot.EventSlot;
import roomescape.domain.waiting.Waiting;
import roomescape.infrastructure.SlotManager;
import roomescape.service.processor.WaitingPromotionProcessor;

@Component
public class EmptyReservationDetectionScheduler {
    private final WaitingDao waitingDao;
    private final SlotManager slotManager;
    private final WaitingPromotionProcessor promotionProcessor;

    public EmptyReservationDetectionScheduler(
            WaitingDao waitingDao,
            SlotManager slotManager,
            WaitingPromotionProcessor promotionProcessor
    ) {
        this.waitingDao = waitingDao;
        this.slotManager = slotManager;
        this.promotionProcessor = promotionProcessor;
    }

    @Scheduled(fixedDelay = 30 * 1000)
    public void processWaitingPromotion() {
        List<EventSlot> emptySlots = waitingDao.findUnreservedWaiting().stream()
                .map(Waiting::getSlot)
                .toList();

        for (EventSlot slot : emptySlots) {
            if (slotManager.tryAcquire(slot)) {
                promotionProcessor.promoteWaiting(slot);
            }
        }
    }
}
