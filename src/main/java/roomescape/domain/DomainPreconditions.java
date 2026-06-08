package roomescape.domain;

public class DomainPreconditions {
    private DomainPreconditions() {
    }

    public static <T> T requireNonNull(final T value, final DomainErrorCode code, final String debugMessage) {
        if (value == null) {
            throw new RoomEscapeException(code, debugMessage);
        }
        return value;
    }

    public static String requireNonBlank(final String value, final DomainErrorCode code, final String debugMessage) {
        if (value == null || value.isBlank()) {
            throw new RoomEscapeException(code, debugMessage);
        }
        return value;
    }

    public static void require(final boolean condition, final DomainErrorCode code, final String debugMessage) {
        if (!condition) {
            throw new RoomEscapeException(code, debugMessage);
        }
    }
}
