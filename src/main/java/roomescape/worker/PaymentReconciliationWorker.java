package roomescape.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.payment.service.PaymentReconciliationService;

/**
 * 결과 불명확(NEEDS_CHECK) 주문 자동 수렴 스케줄러. 사용자가 재시도하지 않아도, 주기적으로 깨어나
 * NEEDS_CHECK 주문을 게이트웨이에 조회해 확정/실패로 수렴시킨다.
 *
 * <p>PromotionOutboxWorker·ExpiredOrderWorker와 같은 폴링 패턴(아웃박스/최종 일관성) — 워커는 얇게
 * 폴링+위임만 하고, 수렴 로직은 PaymentReconciliationService가 소유한다. 한 건이 실패해도(토스 조회 실패 등)
 * 다음 건·다음 주기를 막지 않도록 건별로 격리한다.
 */
@Component
public class PaymentReconciliationWorker {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationWorker.class);

    private final PaymentReconciliationService reconciliationService;

    public PaymentReconciliationWorker(PaymentReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @Scheduled(fixedDelayString = "${payment.reconciliation.poll-interval-ms:60000}")
    public void reconcileUnknownPayments() {
        for (String orderId : reconciliationService.findReconcilableOrderIds()) {
            try {
                reconciliationService.reconcile(orderId);
            } catch (RuntimeException e) {
                log.warn("결제 결과 확인(reconciliation) 실패 (다음 주기 재시도): orderId={}", orderId, e);
            }
        }
    }
}
