package roomescape.domain.exception;

public final class ForbiddenException extends RoomescapeException {

    public static final String NOT_OWNER = "본인의 예약이 아닙니다.";

    public ForbiddenException(String message) {
        super(message);
    }
}
