package roomescape.global.exception;

public class IllegalReservationDateException extends IllegalRequestException {

    public IllegalReservationDateException(String message) {
        super(message);
    }

    public IllegalReservationDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
