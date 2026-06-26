package roomescape.domain;

import java.util.UUID;

/**
 * 주문 = 결제 원장. 주문 생성 시 멱등키를 발급해 고정하고, 승인 시도의 결과(확정/실패/확인필요)를 함께 보관한다.
 * 같은 주문은 재시도해도 항상 같은 idempotencyKey를 사용한다.
 */
public record PaymentOrder(
        String orderId,
        Long amount,
        String idempotencyKey,
        PaymentOrderStatus status,
        String name,
        Long sessionId,
        String paymentKey
) {

    public static PaymentOrder prepare(String orderId, Long amount) {
        return new PaymentOrder(orderId, amount, UUID.randomUUID().toString(),
                PaymentOrderStatus.PENDING, null, null, null);
    }

    public PaymentOrder confirmed(String name, Long sessionId, String paymentKey) {
        return new PaymentOrder(orderId, amount, idempotencyKey,
                PaymentOrderStatus.CONFIRMED, name, sessionId, paymentKey);
    }

    public PaymentOrder failed(String name, Long sessionId) {
        return new PaymentOrder(orderId, amount, idempotencyKey,
                PaymentOrderStatus.FAILED, name, sessionId, paymentKey);
    }

    public PaymentOrder unknown(String name, Long sessionId, String paymentKey) {
        return new PaymentOrder(orderId, amount, idempotencyKey,
                PaymentOrderStatus.UNKNOWN, name, sessionId, paymentKey);
    }

    /** 연결 실패 등 토스에 닿지 못한 경우 — 시도자만 기록하고 재시도 가능한 PENDING으로 유지 */
    public PaymentOrder retryable(String name, Long sessionId) {
        return new PaymentOrder(orderId, amount, idempotencyKey,
                PaymentOrderStatus.PENDING, name, sessionId, paymentKey);
    }

    public boolean isConfirmed() {
        return status == PaymentOrderStatus.CONFIRMED;
    }
}
