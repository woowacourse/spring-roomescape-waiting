package roomescape.exception;

public class ForbiddenReservationException extends RoomescapeException {

    public ForbiddenReservationException(String detail) {
        super(ErrorCode.FORBIDDEN_RESERVATION, detail);
    }
}
