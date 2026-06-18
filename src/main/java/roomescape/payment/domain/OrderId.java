package roomescape.payment.domain;

import java.util.UUID;
import java.util.regex.Pattern;

public record OrderId(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[A-Za-z0-9\\-_]{6,64}$");

    public OrderId {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "orderId는 6~64자의 영숫자, '-', '_'만 허용합니다. (입력값: " + value + ")");
        }
    }

    public static OrderId generate() {
        return new OrderId("ORDER-" + UUID.randomUUID().toString().replace("-", ""));
    }
}
