package roomescape.domain.exception;

public final class RoomescapeException extends RuntimeException {

    private final DomainErrorCode code;
    private final String message;

    public RoomescapeException(DomainErrorCode code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public DomainErrorCode getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
