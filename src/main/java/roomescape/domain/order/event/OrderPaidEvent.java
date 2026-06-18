package roomescape.domain.order.event;

import java.time.LocalDateTime;
import roomescape.common.DomainEvent;
import roomescape.domain.order.OrderType;

public record OrderPaidEvent(
        String orderId,
        Long reservationId,
        OrderType orderType,
        LocalDateTime occurredAt
) implements DomainEvent {

    public OrderPaidEvent(String orderId, Long reservationId, OrderType orderType) {
        this(orderId, reservationId, orderType, LocalDateTime.now());
    }
}
