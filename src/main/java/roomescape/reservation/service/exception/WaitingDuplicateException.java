package roomescape.reservation.service.exception;

public class WaitingDuplicateException extends RuntimeException {

    public WaitingDuplicateException(final String message) {
        super(message);
    }
}
