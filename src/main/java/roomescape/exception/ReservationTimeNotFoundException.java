package roomescape.exception;

public class ReservationTimeNotFoundException extends BusinessException {

    public ReservationTimeNotFoundException() {
        super(ErrorType.RESERVATION_TIME_NOT_FOUND);
    }
}
