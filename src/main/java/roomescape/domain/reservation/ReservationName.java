package roomescape.domain.reservation;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.util.Objects;

public class ReservationName {
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 20;

    private final String value;

    public ReservationName(String value) {
        Objects.requireNonNull(value);
        String striped = value.strip();
        validateLength(striped);
        this.value = striped;
    }

    private void validateLength(String value) {
        if (value.length() < MIN_NAME_LENGTH || value.length() > MAX_NAME_LENGTH) {
            throw new RoomEscapeException(ErrorCode.INVALID_NAME_LENGTH);
        }
    }

    public boolean isSame(String other) {
        return value.equals(other);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationName reservationName = (ReservationName) o;
        return Objects.equals(value, reservationName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
