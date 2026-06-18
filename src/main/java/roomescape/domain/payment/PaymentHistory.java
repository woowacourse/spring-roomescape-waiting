package roomescape.domain.payment;

import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.common.BaseDomain;
import roomescape.domain.payment.event.PaymentApprovedEvent;
import roomescape.exception.PaymentException;

@Getter
public class PaymentHistory extends BaseDomain {

    private final Long id;
    private final String orderId;
    private final String paymentKey;
    private final Long amount;
    private final PaymentStatus status;
    private final LocalDateTime createdAt;

    public PaymentHistory(
            Long id,
            String orderId,
            String paymentKey,
            Long amount,
            PaymentStatus status,
            LocalDateTime createdAt
    ) {
        validateRequired(orderId, "주문 ID는 필수 값입니다.");
        validateRequired(paymentKey, "결제 키 정보는 필수 값입니다.");
        validateAmount(amount);
        validateStatus(status);
        validateCreatedAt(createdAt);

        this.id = id;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static PaymentHistory approved(
            String orderId,
            String paymentKey,
            Long amount,
            PaymentStatus status
    ) {
        PaymentHistory paymentHistory = new PaymentHistory(
                null,
                orderId,
                paymentKey,
                amount,
                status,
                LocalDateTime.now()
        );
        paymentHistory.addEvent(new PaymentApprovedEvent(orderId, paymentKey, amount, status));
        return paymentHistory;
    }

    private static void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new PaymentException(message);
        }
    }

    private static void validateAmount(Long amount) {
        if (amount == null || amount <= 0) {
            throw new PaymentException("결제 금액은 양수여야 합니다.");
        }
    }

    private static void validateStatus(PaymentStatus status) {
        if (status == null) {
            throw new PaymentException("결제 상태는 필수 값입니다.");
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new PaymentException("결제 이력 생성 시각은 필수 값입니다.");
        }
    }
}
