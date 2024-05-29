package roomescape.reservation.exception;

public class DataNotConvertedException extends RuntimeException {
    private final String message;

    public DataNotConvertedException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
