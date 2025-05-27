package roomescape.common.exception;

public class WaitingNotAllowedException extends RuntimeException {
    public WaitingNotAllowedException(String message) {
        super(message);
    }
}
