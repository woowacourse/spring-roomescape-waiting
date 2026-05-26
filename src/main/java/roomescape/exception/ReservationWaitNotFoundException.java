package roomescape.exception;

public class ReservationWaitNotFoundException extends BusinessException {

    public ReservationWaitNotFoundException() {
        super(ErrorType.RESERVATION_WAIT_NOT_FOUND);
    }
}
