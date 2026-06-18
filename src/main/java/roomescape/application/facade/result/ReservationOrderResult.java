package roomescape.application.facade.result;

import roomescape.application.service.result.OrderResult;
import roomescape.domain.order.OrderType;

public record ReservationOrderResult(
        String orderId,
        String orderName,
        Long amount,
        OrderType orderType
) {

    public static ReservationOrderResult from(OrderResult order) {
        return new ReservationOrderResult(
                order.orderId(),
                order.orderName(),
                order.amount(),
                order.orderType()
        );
    }
}
