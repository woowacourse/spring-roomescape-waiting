package roomescape.reservation.domain;

import java.util.UUID;
import java.util.regex.Pattern;
import lombok.Builder;
import roomescape.global.exception.RoomEscapeException;

public record OrderId(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[A-Za-z0-9_-]{6,64}$");

    @Builder
    public OrderId {
        validateValue(value);
    }

    public static OrderId generate() {
        return OrderId.builder()
                .value("order-" + UUID.randomUUID().toString().replace("-", ""))
                .build();
    }

    private static void validateValue(String value) {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new RoomEscapeException("주문 ID는 6~64자의 영숫자, -, _만 사용할 수 있습니다.");
        }
    }
}
