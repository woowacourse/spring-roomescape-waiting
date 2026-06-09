package roomescape.domain;

public class RoomEscapeException extends IllegalArgumentException {

    private final DomainErrorCode code;

    public RoomEscapeException(DomainErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public DomainErrorCode code() {
        return code;
    }
}
