package roomescape.domain.reservation;

import java.util.Objects;

public class ReservationName {
    private static final int MAX_LENGTH = 10;

    private final String value;

    private ReservationName(final String name) {
        validate(name);
        this.value = name.trim();
    }

    public static ReservationName from(final String name) {
        return new ReservationName(name);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ReservationName)) {
            return false;
        }
        ReservationName that = (ReservationName) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    private void validate(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수입니다.");
        }

        if (name.trim().length() >= MAX_LENGTH) {
            throw new IllegalArgumentException("예약자 이름은 10자 미만이어야 합니다.");
        }
    }
}
