package roomescape.domain;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;

public class OrderId {

    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 64;
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]+$");

    private final String value;

    private OrderId(String value) {
        validate(value);
        this.value = value;
    }

    public static OrderId generate() {
        String generated = UUID.randomUUID().toString();
        return new OrderId(generated);
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "주문 번호는 필수입니다.");
        }
        int length = value.length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN,
                    "주문 번호는 " + MIN_LENGTH + "~" + MAX_LENGTH + "자여야 합니다. 입력 길이: " + length);
        }
        if (!ALLOWED_PATTERN.matcher(value).matches()) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN,
                    "주문 번호는 영문, 숫자, '-', '_'만 사용할 수 있습니다. 입력 값: " + value);
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderId orderId)) {
            return false;
        }
        return Objects.equals(value, orderId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
