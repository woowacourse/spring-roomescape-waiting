package roomescape.exception;

public class NameNotFoundException extends IllegalArgumentException {
    public NameNotFoundException(String message) {
        super(message);
    }
}
