package payment.order;

import java.time.LocalDateTime;
import payment.PaymentStatus;

/**
 * 결제 전에 서버가 미리 저장해 두는 주문 정보. successUrl 의 amount 와 대조해 금액 위변조를 막는 기준값이다.
 */
public record Order(
        Long id,
        String orderId,
        Long reservationId,
        Long amount,
        String paymentKey,
        PaymentStatus status,
        LocalDateTime createdAt
) {

    public static Order ready(String orderId, Long reservationId, Long amount, LocalDateTime createdAt) {
        return new Order(null, orderId, reservationId, amount, null, PaymentStatus.READY, createdAt);
    }
}
