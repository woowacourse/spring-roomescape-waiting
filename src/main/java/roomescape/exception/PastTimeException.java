package roomescape.exception;

public class PastTimeException extends RoomescapeException {

    public PastTimeException(String message) {
        super("PAST_TIME", message);
    }
}
