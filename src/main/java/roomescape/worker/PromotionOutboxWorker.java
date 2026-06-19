package roomescape.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.promotion.PromotionService;
import roomescape.promotion.PromotionTask;

/**
 * 아웃박스 워커. 이벤트 기반(push)이 아니라 시간 기반(polling)으로, 일정 간격마다 깨어나
 * PENDING 할 일을 주워 PromotionService에 처리를 위임한다. (영속 접근은 서비스가 책임진다.)
 * 한 건 처리가 실패하면 PENDING으로 남고, 다음 주기에 자연히 재시도된다.
 */
@Component
public class PromotionOutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(PromotionOutboxWorker.class);

    private final PromotionService promotionService;

    public PromotionOutboxWorker(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @Scheduled(fixedDelayString = "${promotion.poll-interval-ms:5000}")
    public void processPendingTasks() {
        for (PromotionTask task : promotionService.findPendingTasks()) {
            process(task);
        }
    }

    private void process(PromotionTask task) {
        try {
            promotionService.processTask(task);
        } catch (RuntimeException e) {
            // 일시적 실패: PENDING으로 남겨 다음 주기에 재시도한다. 한 건 실패가 나머지를 막지 않도록 격리한다.
            log.warn("승격 아웃박스 처리 실패 (재시도 예정): taskId={}", task.getId(), e);
        }
    }
}
