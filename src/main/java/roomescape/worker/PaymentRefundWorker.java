package roomescape.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.payment.service.PaymentRefundService;

/**
 * 환불(보상) 자동 처리 스케줄러. 결제는 됐지만 예약 확정에 실패한(NEEDS_REFUND) 주문을 주기적으로 주워
 * 게이트웨이 취소로 환불한다. PaymentReconciliationWorker와 같은 폴링 패턴(아웃박스/최종 일관성) —
 * 워커는 얇게 폴링+위임만 하고, 보상 로직은 PaymentRefundService가 소유한다. 한 건이 실패해도(취소 불명확 등)
 * 다음 건·다음 주기를 막지 않도록 건별로 격리한다(멱등키라 재시도가 안전).
 */
@Component
public class PaymentRefundWorker {

    private static final Logger log = LoggerFactory.getLogger(PaymentRefundWorker.class);

    private final PaymentRefundService refundService;

    public PaymentRefundWorker(PaymentRefundService refundService) {
        this.refundService = refundService;
    }

    @Scheduled(fixedDelayString = "${payment.refund.poll-interval-ms:60000}")
    public void refundUnsecuredPayments() {
        for (String orderId : refundService.findRefundableOrderIds()) {
            try {
                refundService.refund(orderId);
            } catch (RuntimeException e) {
                log.warn("환불(보상) 처리 실패 (다음 주기 재시도): orderId={}", orderId, e);
            }
        }
    }
}
