package roomescape.exception;

public class ReservationTimeInUseException extends BusinessException {

    public ReservationTimeInUseException() {
        super(ErrorType.RESERVATION_TIME_IN_USE);
    }
}
