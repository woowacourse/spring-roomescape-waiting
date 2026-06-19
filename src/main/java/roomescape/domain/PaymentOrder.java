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

    private PaymentOrder(Long id, String orderId, Long amount, Long entryId, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.entryId = entryId;
        this.createdAt = createdAt;
    }

    public static PaymentOrder create(String orderId, Long amount, Long entryId, LocalDateTime createdAt) {
        return new PaymentOrder(null, orderId, amount, entryId, createdAt);
    }

    public static PaymentOrder restore(Long id, String orderId, Long amount, Long entryId, LocalDateTime createdAt) {
        return new PaymentOrder(id, orderId, amount, entryId, createdAt);
    }

    public PaymentOrder withId(Long id) {
        return new PaymentOrder(id, orderId, amount, entryId, createdAt);
    }
}
