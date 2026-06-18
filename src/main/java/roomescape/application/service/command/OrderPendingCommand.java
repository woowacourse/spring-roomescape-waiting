package roomescape.application.service.command;

import roomescape.domain.order.OrderAmount;
import roomescape.domain.order.OrderName;
import roomescape.domain.order.OrderType;

public record OrderPendingCommand(
        Long targetId,
        OrderType orderType,
        OrderName orderName,
        OrderAmount amount
) {
}
