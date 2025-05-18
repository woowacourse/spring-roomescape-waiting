package roomescape.reservation.domain.util;

public class ValidationUtils {

    public static void validateNonNull(final Object value, final String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void validateNonBlank(final String value, final String message) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
