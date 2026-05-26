package roomescape.exception;

public class ReservationTimeException extends CustomException {
    public ReservationTimeException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
