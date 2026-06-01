package roomescape.exception;

public class PastReservationLockedException extends RoomescapeException {

    public PastReservationLockedException(String detail) {
        super(ErrorCode.PAST_RESERVATION_LOCKED, detail);
    }
}
