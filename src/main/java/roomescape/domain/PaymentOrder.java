package roomescape.domain;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PaymentOrder {

    private final Long id;
    private final String orderId;
    private final Long amount;
    private final Long entryId;
    private final LocalDateTime createdAt;
    private final String paymentKey;
    private final PaymentOrderStatus status;

    private PaymentOrder(Long id, String orderId, Long amount, Long entryId, LocalDateTime createdAt,
                         String paymentKey, PaymentOrderStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.entryId = entryId;
        this.createdAt = createdAt;
        this.paymentKey = paymentKey;
        this.status = status;
    }

    public static PaymentOrder create(String orderId, Long amount, Long entryId, LocalDateTime createdAt) {
        return new PaymentOrder(null, orderId, amount, entryId, createdAt, null, PaymentOrderStatus.PENDING);
    }

    public static PaymentOrder restore(Long id, String orderId, Long amount, Long entryId, LocalDateTime createdAt,
                                       String paymentKey, PaymentOrderStatus status) {
        return new PaymentOrder(id, orderId, amount, entryId, createdAt, paymentKey, status);
    }

    public PaymentOrder withId(Long id) {
        return new PaymentOrder(id, orderId, amount, entryId, createdAt, paymentKey, status);
    }

    public PaymentOrder confirmed(String paymentKey) {
        return new PaymentOrder(id, orderId, amount, entryId, createdAt, paymentKey, PaymentOrderStatus.CONFIRMED);
    }

    public PaymentOrder failed() {
        return new PaymentOrder(id, orderId, amount, entryId, createdAt, paymentKey, PaymentOrderStatus.FAILED);
    }

    public PaymentOrder resultUnknown() {
        return new PaymentOrder(id, orderId, amount, entryId, createdAt, paymentKey,
                PaymentOrderStatus.CONFIRM_RESULT_UNKNOWN);
    }
}
