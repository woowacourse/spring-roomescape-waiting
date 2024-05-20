package roomescape.global.exception;

public class ReservationTimeNotHourlyUnitException extends IllegalRequestException {

    public ReservationTimeNotHourlyUnitException(String message) {
        super(message);
    }

    public ReservationTimeNotHourlyUnitException(String message, Throwable cause) {
        super(message, cause);
    }
}
