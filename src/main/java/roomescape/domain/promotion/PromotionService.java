package roomescape.domain.promotion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.PromotionOutboxDao;
import roomescape.reservation.ReservationDao;
import roomescape.waiting.WaitingDao;
import roomescape.common.vo.Slot;

/**
 * 대기 승격 오케스트레이션. 대기 신청/조회/취소(WaitingService)와 분리해, 승격이라는 관심사만 모은다.
 * 취소 흐름은 enqueuePromotion으로 할 일만 기록하고, 워커는 findPendingTasks/processTask로 실제 승격을 멱등하게 수행한다.
 */
@Service
@Transactional
public class PromotionService {
    private final PromotionOutboxDao promotionOutboxDao;
    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;

    public PromotionService(PromotionOutboxDao promotionOutboxDao, WaitingDao waitingDao,
                            ReservationDao reservationDao) {
        this.promotionOutboxDao = promotionOutboxDao;
        this.waitingDao = waitingDao;
        this.reservationDao = reservationDao;
    }

    /**
     * 취소 트랜잭션 안에서 호출된다. inline으로 승격하지 않고, "이 슬롯의 다음 대기자를 승격시켜라"라는 할 일만
     * 아웃박스에 기록한다(취소와 같은 트랜잭션 → 원자적). 실제 승격은 워커가 나중에 수행한다.
     */
    public void enqueuePromotion(Slot slot) {
        promotionOutboxDao.insert(PromotionTask.pending(slot));
    }

    @Transactional(readOnly = true)
    public List<PromotionTask> findPendingTasks() {
        return promotionOutboxDao.findByStatus(OutboxStatus.PENDING);
    }

    /**
     * 아웃박스 할 일 한 건을 한 트랜잭션으로 처리한다. 승격과 완료 표시(markDone)가 함께 커밋되거나 함께 롤백되며,
     * 실패하면 PENDING으로 남아 다음 주기에 멱등하게 재시도된다.
     */
    public void processTask(PromotionTask task) {
        promotePendingSlot(task.getThemeId(), task.getTimeId(), task.getDate(), task.getStoreId());
        promotionOutboxDao.markDone(task.getId());
    }

    /**
     * 여러 번 실행되어도 결과가 같도록 멱등하게 설계했다:
     * 이미 해당 슬롯에 활성 예약이 있으면(=이미 승격됨 또는 그새 누가 예약함) 아무것도 하지 않는다.
     */
    private void promotePendingSlot(Long themeId, Long timeId, LocalDate date, Long storeId) {
        if (reservationDao.findBySlotKeyForUpdate(themeId, timeId, date, storeId).isPresent()) {
            return;
        }
        waitingDao.findFirstBySlotKeyForUpdate(themeId, timeId, date, storeId)
                .ifPresent(first -> {
                    if (first.isPast(LocalDateTime.now())) {
                        return;
                    }
                    reservationDao.insert(first.promote());
                    waitingDao.delete(first.getId());
                });
    }
}
