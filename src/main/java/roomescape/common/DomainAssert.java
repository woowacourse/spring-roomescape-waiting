package roomescape.common;

import roomescape.common.exception.InvalidInputException;

public final class DomainAssert {

    private DomainAssert() {
    }

    public static <T> T notNull(T value, String message) {
        if (value == null) {
            throw new InvalidInputException(message);
        }
        return value;
    }
}
