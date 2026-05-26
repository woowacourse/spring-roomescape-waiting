package roomescape.common.domain;

public class DomainPreconditions {

    private DomainPreconditions() {
    }

    public static <T> void requireNonNull(
            final T value,
            final RuntimeException exception
    ) {
        if (value == null) {
            throw exception;
        }
    }

    public static <T> void requireNonBlank(
            final String value,
            final RuntimeException exception
    ) {
        if (value == null || value.isBlank()) {
            throw exception;
        }
    }


    public static void require(
            final boolean condition,
            final RuntimeException exception
    ) {
        if (!condition) {
            throw exception;
        }
    }

}
