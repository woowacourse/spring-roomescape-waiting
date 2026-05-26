package roomescape.exception;

public class ReservationAlreadyExistsException extends BusinessException {

    public ReservationAlreadyExistsException() {
        super(ErrorType.RESERVATION_ALREADY_EXISTS);
    }
}
