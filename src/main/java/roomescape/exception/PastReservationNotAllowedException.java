package roomescape.exception;

public class PastReservationNotAllowedException extends BusinessException {

    public PastReservationNotAllowedException() {
        super(ErrorType.PAST_RESERVATION_NOT_ALLOWED);
    }
}
