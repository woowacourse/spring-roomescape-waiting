package roomescape.domain.exception;

public final class DomainPreconditions {

    private DomainPreconditions() {
    }

    public static <T> T requireNonNull(T value, DomainErrorCode code, String debugMessage) {
        if (value == null) {
            throw new RoomEscapeException(code, debugMessage);
        }
        return value;
    }

    public static String requireNonBlank(String value, DomainErrorCode code, String debugMessage) {
        if (value == null || value.isBlank()) {
            throw new RoomEscapeException(code, debugMessage);
        }
        return value;
    }

    public static void require(boolean condition, DomainErrorCode code, String debugMessage) {
        if (!condition) {
            throw new RoomEscapeException(code, debugMessage);
        }
    }
}
