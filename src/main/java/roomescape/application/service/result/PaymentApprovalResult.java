package roomescape.application.service.result;

import roomescape.domain.order.OrderType;
import roomescape.pg.PaymentResult;

public record PaymentApprovalResult(
        PaymentResult payment,
        OrderType orderType,
        Long targetId
) {
}
