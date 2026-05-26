package roomescape.exception;

public class PastReservationWaitNotAllowedException extends BusinessException {
    public PastReservationWaitNotAllowedException() {
        super(ErrorType.PAST_RESERVATION_WAIT_NOT_ALLOWED);
    }
}
