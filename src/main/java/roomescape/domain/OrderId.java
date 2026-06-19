package roomescape.domain;

import java.util.UUID;

public record OrderId(
        String id
) {

    private static final String PREFIX = "order-";

    public OrderId {
        validate(id);
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
