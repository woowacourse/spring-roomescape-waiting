package roomescape.exception;

public class IllegalReservationDateTimeRequestException extends BadRequestException {

    public IllegalReservationDateTimeRequestException(String message) {
        super(message);
    }

}
