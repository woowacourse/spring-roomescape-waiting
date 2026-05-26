package roomescape.exception;

public class ReservationNotFoundException extends BusinessException {

    public ReservationNotFoundException() {
        super(ErrorType.RESERVATION_NOT_FOUND);
    }
}
