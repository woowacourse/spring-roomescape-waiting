package roomescape.domain.order;

import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.common.BaseDomain;
import roomescape.domain.order.event.OrderFailedEvent;
import roomescape.domain.order.event.OrderPaidEvent;
import roomescape.exception.OrderException;

@Getter
public class Order extends BaseDomain {

    private final OrderId orderId;
    private final Long targetId;
    private final OrderType orderType;
    private final OrderName orderName;
    private final OrderAmount amount;
    private final LocalDateTime createdAt;
    private OrderStatus status;

    public Order(
            OrderId orderId,
            Long targetId,
            OrderType orderType,
            OrderName orderName,
            OrderAmount amount,
            OrderStatus status,
            LocalDateTime createdAt
    ) {
        validateTargetId(targetId);
        validateOrderType(orderType);
        validateStatus(status);
        validateCreatedAt(createdAt);

        this.orderId = orderId;
        this.targetId = targetId;
        this.orderType = orderType;
        this.orderName = orderName;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Order pending(
            Long targetId,
            OrderType orderType,
            OrderName orderName,
            OrderAmount amount
    ) {
        return new Order(
                OrderId.generate(),
                targetId,
                orderType,
                orderName,
                amount,
                OrderStatus.PENDING,
                LocalDateTime.now()
        );
    }

    private static void validateTargetId(Long targetId) {
        if (targetId == null || targetId <= 0) {
            throw new OrderException("주문 대상 식별자는 양수여야 합니다.");
        }
    }

    private static void validateOrderType(OrderType orderType) {
        if (orderType == null) {
            throw new OrderException("주문 타입은 필수 값입니다.");
        }
    }

    private static void validateStatus(OrderStatus status) {
        if (status == null) {
            throw new OrderException("주문 상태는 필수 값입니다.");
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new OrderException("주문 생성 시각은 필수 값입니다.");
        }
    }

    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }

    public boolean isWaiting() {
        return orderType == OrderType.WAITING;
    }

    public void paid() {
        this.status = OrderStatus.PAID;
        addEvent(new OrderPaidEvent(orderId.value(), targetId, orderType));
    }

    public void failed() {
        this.status = OrderStatus.FAILED;
        addEvent(new OrderFailedEvent(orderId.value(), targetId));
    }

}
