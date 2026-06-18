package roomescape.domain.order;

import java.util.UUID;
import roomescape.exception.OrderException;

public record OrderId(String value) {

    private static final String PREFIX = "reservation-";

    public OrderId {
        if (value == null || value.isBlank()) {
            throw new OrderException("주문번호는 필수 값입니다.");
        }
    }

    public static OrderId generate() {
        return new OrderId(PREFIX + UUID.randomUUID().toString().replace("-", ""));
    }
}
