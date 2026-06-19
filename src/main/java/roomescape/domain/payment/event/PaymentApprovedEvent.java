package roomescape.domain.payment.event;

import java.time.LocalDateTime;
import roomescape.common.DomainEvent;
import roomescape.domain.payment.PaymentStatus;

public record PaymentApprovedEvent(
        String orderId,
        String paymentKey,
        Long amount,
        PaymentStatus status,
        LocalDateTime occurredAt
) implements DomainEvent {

    public PaymentApprovedEvent(String orderId, String paymentKey, Long amount, PaymentStatus status) {
        this(orderId, paymentKey, amount, status, LocalDateTime.now());
    }
}
