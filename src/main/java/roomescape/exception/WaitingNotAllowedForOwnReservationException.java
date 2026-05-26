package roomescape.exception;

public class WaitingNotAllowedForOwnReservationException extends RoomescapeException {
    public WaitingNotAllowedForOwnReservationException(String detail) {
        super(ErrorCode.WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION, detail);
    }
}
