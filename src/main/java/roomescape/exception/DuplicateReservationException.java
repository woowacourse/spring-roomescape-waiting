package roomescape.exception;

public class DuplicateReservationException extends RoomescapeException {

    public DuplicateReservationException(String detail) {
        super(ErrorCode.DUPLICATE_RESERVATION, detail);
    }
}
