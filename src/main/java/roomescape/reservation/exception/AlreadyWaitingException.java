package roomescape.reservation.exception;

public class AlreadyWaitingException extends RuntimeException {
    public AlreadyWaitingException(String message) {
        super(message);
    }
}
