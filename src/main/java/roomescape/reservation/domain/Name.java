package roomescape.reservation.domain;

import java.util.Objects;

public class Name {

    private static final int MAX_NAME_LENGTH = 25;

    private final String value;

    public Name(final String value) {
        validateNameValue(value);
        this.value = value;
    }

    private void validateNameValue(final String value) {
        if (value == null || value.isBlank() || value.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("invalid reservation value");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Name name)) {
            return false;
        }
        return Objects.equals(getValue(), name.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }
}
