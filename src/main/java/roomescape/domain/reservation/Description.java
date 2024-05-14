package roomescape.domain.reservation;

import roomescape.exception.InvalidReservationException;

import java.util.Objects;

public class Description {
    private static final int MAXIMUM_DESCRIPTION_LENGTH = 100;
    private static final String INVALID_DESCRIPTION_LENGTH = String.format("설명은 %d자를 초과할 수 없습니다.", MAXIMUM_DESCRIPTION_LENGTH);

    private final String value;

    public Description(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value.length() > MAXIMUM_DESCRIPTION_LENGTH) {
            throw new InvalidReservationException(INVALID_DESCRIPTION_LENGTH);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Description that = (Description) o;
        return Objects.equals(value, that.value);
    }

    public String getValue() {
        return value;
    }
}
