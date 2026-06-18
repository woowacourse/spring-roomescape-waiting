package roomescape.application.service.result;

import roomescape.domain.order.Order;
import roomescape.domain.order.OrderType;

public record OrderResult(
        String orderId,
        String orderName,
        Long amount,
        OrderType orderType
) {

    public static OrderResult from(Order order) {
        return new OrderResult(
                order.getOrderId().value(),
                order.getOrderName().value(),
                order.getAmount().value(),
                order.getOrderType()
        );
    }
}
