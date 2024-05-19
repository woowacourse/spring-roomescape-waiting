package roomescape.domain;

import static roomescape.exception.ExceptionType.INVALID_NAME;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.exception.RoomescapeException;

@Embeddable
public class Name {
    private String value;

    public Name(String value) {
        validateValue(value);
        this.value = value;
    }

    public Name() {

    }

    private void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new RoomescapeException(INVALID_NAME);
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Name name = (Name) o;
        return Objects.equals(value, name.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "Name{" +
                "value='" + value + '\'' +
                '}';
    }
}
