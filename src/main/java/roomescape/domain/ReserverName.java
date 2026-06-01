package roomescape.domain;

import java.util.Objects;

public class ReserverName {

    private static final int MAX_LENGTH = 10;

    private final String value;

    private ReserverName(final String value) {
        this.value = value;
    }

    public static ReserverName from(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 비어 있을 수 없습니다.");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() >= MAX_LENGTH) {
            throw new IllegalArgumentException("예약자 이름은 10자 미만이어야 합니다.");
        }

        return new ReserverName(trimmedName);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ReserverName that)) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
