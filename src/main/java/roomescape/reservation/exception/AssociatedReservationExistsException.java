package roomescape.reservation.exception;

public class AssociatedReservationExistsException extends RuntimeException {

    public AssociatedReservationExistsException(String message) {
        super(message);
    }
}
