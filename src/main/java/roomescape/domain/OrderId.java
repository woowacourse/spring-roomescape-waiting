package roomescape.domain;

import lombok.Getter;

import java.util.UUID;

@Getter
public class OrderId {

    private static final String PREFIX = "order-";

    private final String id;

    public OrderId(final String id) {
        validate(id);
        this.id = id;
    }

    public static OrderId generate() {
        final String randomOrderId = PREFIX + UUID.randomUUID();
        return new OrderId(randomOrderId);
    }

    private void validate(String id) {
        if (id.startsWith(PREFIX)) {
            return;
        }
        throw new IllegalArgumentException("주문 번호는 order- 로 시작해야 합니다.");
    }
}
