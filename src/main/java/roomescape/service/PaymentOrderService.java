package roomescape.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.PaymentOrder;
import roomescape.repository.PaymentOrderRepository;

@Service
@Transactional(readOnly = true)
public class PaymentOrderService {

    private final PaymentOrderRepository paymentOrderRepository;

    public PaymentOrderService(PaymentOrderRepository paymentOrderRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
    }

    @Transactional
    public PaymentOrder prepare(Long amount) {
        String orderId = UUID.randomUUID().toString();
        return paymentOrderRepository.save(PaymentOrder.prepare(orderId, amount));
    }

    @Transactional
    public void cancel(String orderId) {
        paymentOrderRepository.deleteByOrderId(orderId);
    }

    /** 승인 성공 기록. 호출자(예약 저장)의 트랜잭션에 합류해 예약 저장과 원자적으로 커밋된다. */
    @Transactional
    public PaymentOrder confirm(PaymentOrder order, String name, Long sessionId, String paymentKey) {
        return paymentOrderRepository.update(order.confirmed(name, sessionId, paymentKey));
    }

    public PaymentOrder getByOrderId(String orderId) {
        return paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다: " + orderId));
    }

    public List<PaymentOrder> findByName(String name) {
        return paymentOrderRepository.findByName(name);
    }

    /**
     * 승인 시도 실패를 별도 트랜잭션으로 기록한다. makeReservation 트랜잭션이 예외로 롤백되더라도
     * "확인 필요(UNKNOWN)" / "실패(FAILED)" / 재시도 가능(PENDING) 상태는 남겨야 하기 때문이다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordUnknown(String orderId, String name, Long sessionId, String paymentKey) {
        paymentOrderRepository.update(getByOrderId(orderId).unknown(name, sessionId, paymentKey));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailed(String orderId, String name, Long sessionId) {
        paymentOrderRepository.update(getByOrderId(orderId).failed(name, sessionId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRetryable(String orderId, String name, Long sessionId) {
        paymentOrderRepository.update(getByOrderId(orderId).retryable(name, sessionId));
    }
}
