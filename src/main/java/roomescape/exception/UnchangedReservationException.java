package roomescape.exception;

public class UnchangedReservationException extends RoomescapeException {

    public UnchangedReservationException(String detail) {
        super(ErrorCode.UNCHANGED_RESERVATION, detail);
    }
}
