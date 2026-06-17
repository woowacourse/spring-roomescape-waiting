package roomescape.common.vo;

import java.util.Objects;
import roomescape.common.exception.InvalidInputException;

public class Name {
    private static final int MAX_LENGTH = 15;
    private static final int MIN_LENGTH = 2;
    private final String value;

    public Name(String value) {
        validateLength(value);
        this.value = value;
    }

    private void validateLength(String name) {
        if (name.isBlank() || name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw new InvalidInputException("이름은 2~15글자만 가능합니다.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Name name1)) {
            return false;
        }

        return Objects.equals(value, name1.value);
    }

    @Override
    public String toString() {
        return value;
    }
}

