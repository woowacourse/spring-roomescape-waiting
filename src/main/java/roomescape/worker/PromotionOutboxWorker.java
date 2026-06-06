package roomescape.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.dao.PromotionOutboxDao;
import roomescape.domain.OutboxStatus;
import roomescape.domain.PromotionTask;
import roomescape.service.PromotionService;

/**
 * 아웃박스 워커. 이벤트 기반(push)이 아니라 시간 기반(polling)으로,
 * 일정 간격마다 깨어나 PENDING 할 일을 주워 승격을 실행한다.
 * 승격이 실패하면 markDone에 도달하지 못해 PENDING으로 남고, 다음 주기에 자연히 재시도된다.
 */
@Component
public class PromotionOutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(PromotionOutboxWorker.class);

    private final PromotionOutboxDao promotionOutboxDao;
    private final PromotionService promotionService;

    public PromotionOutboxWorker(PromotionOutboxDao promotionOutboxDao, PromotionService promotionService) {
        this.promotionOutboxDao = promotionOutboxDao;
        this.promotionService = promotionService;
    }

    @Scheduled(fixedDelayString = "${promotion.poll-interval-ms:5000}")
    public void processPendingTasks() {
        for (PromotionTask task : promotionOutboxDao.findByStatus(OutboxStatus.PENDING)) {
            process(task);
        }
    }

    private void process(PromotionTask task) {
        try {
            promotionService.promotePendingSlot(
                    task.getThemeId(), task.getTimeId(), task.getDate(), task.getStoreId());
            promotionOutboxDao.markDone(task.getId());
        } catch (RuntimeException e) {
            // 일시적 실패: PENDING으로 남겨 다음 주기에 재시도한다. 한 건 실패가 나머지를 막지 않도록 격리한다.
            log.warn("승격 아웃박스 처리 실패 (재시도 예정): taskId={}", task.getId(), e);
        }
    }
}
