package roomescape.exception;

public class ReservationOwnerMismatchException extends BusinessException {

    public ReservationOwnerMismatchException() {
        super(ErrorType.RESERVATION_OWNER_MISMATCH);
    }
}
