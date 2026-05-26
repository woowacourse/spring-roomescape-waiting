package roomescape.exception;

public class PastReservationCancelNotAllowedException extends BusinessException {

    public PastReservationCancelNotAllowedException() {
        super(ErrorType.PAST_RESERVATION_CANCEL_NOT_ALLOWED);
    }
}
