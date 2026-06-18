package roomescape.domain.order;

import roomescape.exception.OrderException;

public record OrderName(String value) {

    public OrderName {
        if (value == null || value.isBlank()) {
            throw new OrderException("주문명은 필수 값입니다.");
        }
    }

    public static OrderName from(String targetName, OrderType orderType) {
        if (targetName == null || targetName.isBlank()) {
            throw new OrderException("주문 대상 이름은 필수 값입니다.");
        }
        return new OrderName(targetName + " " + orderType.label());
    }
}
