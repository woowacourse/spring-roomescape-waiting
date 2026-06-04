package roomescape.feature.reservation.cancel;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.feature.reservation.domain.Slot;
import roomescape.feature.reservation.repository.ReservationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadSlotPromotionScheduler {

    private final ReservationRepository reservationRepository;
    private final WaitingPromoter waitingPromoter;

    @Scheduled(cron = "0 0 * * * *")
    @SchedulerLock(name = "promoteDeadSlots", lockAtLeastFor = "PT30S", lockAtMostFor = "PT10M")
    public void promoteDeadSlots() {
        List<Slot> deadSlots = reservationRepository.findDeadSlots();
        if (deadSlots.isEmpty()) {
            return;
        }

        log.info("죽은 슬롯 {}건을 발견하여 대기 승격을 시도합니다.", deadSlots.size());
        for (Slot deadSlot : deadSlots) {
            try {
                waitingPromoter.promoteFastestWaiting(deadSlot);
            } catch (Exception exception) {
                log.error(
                        "죽은 슬롯 대기 승격에 실패했습니다. date={}, timeId={}, themeId={}",
                        deadSlot.getDate(), deadSlot.getTimeId(), deadSlot.getThemeId(), exception
                );
            }
        }
    }
}
