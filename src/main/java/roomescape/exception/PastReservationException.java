package roomescape.exception;

public class PastReservationException extends RoomescapeException {

    public PastReservationException(String detail) {
        super(ErrorCode.PAST_RESERVATION, detail);
    }
}
