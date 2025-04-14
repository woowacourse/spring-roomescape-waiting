package roomescape.reservation.domain.exception;

public class ReserverNameEmptyException extends RuntimeException {
    public ReserverNameEmptyException(String message) {
        super(message);
    }
}
