package roomescape.controller.client.api.dto.response;

import roomescape.application.facade.result.ReservationOrderResult;
import roomescape.application.service.result.OrderResult;
import roomescape.domain.order.OrderType;

public record OrderResponse(
        String orderId,
        String orderName,
        Long amount,
        OrderType orderType
) {
    public static OrderResponse from(OrderResult result) {
        return new OrderResponse(
                result.orderId(),
                result.orderName(),
                result.amount(),
                result.orderType()
        );
    }

    public static OrderResponse from(ReservationOrderResult result) {
        return new OrderResponse(
                result.orderId(),
                result.orderName(),
                result.amount(),
                result.orderType()
        );
    }
}
