package roomescape.domain.order.event;

import java.time.LocalDateTime;
import roomescape.common.DomainEvent;

public record OrderFailedEvent(
        String orderId,
        Long reservationId,
        LocalDateTime occurredAt
) implements DomainEvent {

    public OrderFailedEvent(String orderId, Long reservationId) {
        this(orderId, reservationId, LocalDateTime.now());
    }
}
