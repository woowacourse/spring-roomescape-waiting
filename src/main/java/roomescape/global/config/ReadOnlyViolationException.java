package roomescape.global.config;

public class ReadOnlyViolationException extends UnsupportedOperationException {
    public ReadOnlyViolationException(String message) {
        super(message);
    }
}
