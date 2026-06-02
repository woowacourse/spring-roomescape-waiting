package roomescape.util;

public final class Validator {

    private Validator() {
    }

    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void positive(Long value, String message) {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
