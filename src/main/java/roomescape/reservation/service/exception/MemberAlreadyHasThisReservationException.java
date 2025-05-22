package roomescape.reservation.service.exception;

public class MemberAlreadyHasThisReservationException extends RuntimeException {

    public MemberAlreadyHasThisReservationException(final String message) {
        super(message);
    }
}
