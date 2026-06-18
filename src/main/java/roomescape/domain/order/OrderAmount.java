package roomescape.domain.order;

import roomescape.exception.OrderException;

public record OrderAmount(long value) {

    public OrderAmount {
        if (value <= 0) {
            throw new OrderException("주문 금액은 양수여야 합니다.");
        }
    }

    public static OrderAmount valueOf(int amount) {
        return new OrderAmount(amount);
    }
}
