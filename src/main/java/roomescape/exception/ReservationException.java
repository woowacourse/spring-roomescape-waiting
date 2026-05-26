package roomescape.exception;

public class ReservationException extends CustomException {
    public ReservationException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
