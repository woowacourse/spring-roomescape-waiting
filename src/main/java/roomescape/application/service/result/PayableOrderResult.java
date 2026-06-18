package roomescape.application.service.result;

import roomescape.domain.order.Order;
import roomescape.domain.order.OrderAmount;
import roomescape.domain.order.OrderType;

public record PayableOrderResult(
        String orderId,
        OrderAmount amount,
        OrderType orderType,
        Long targetId
) {

    public static PayableOrderResult from(Order order) {
        return new PayableOrderResult(
                order.getOrderId().value(),
                order.getAmount(),
                order.getOrderType(),
                order.getTargetId()
        );
    }
}
