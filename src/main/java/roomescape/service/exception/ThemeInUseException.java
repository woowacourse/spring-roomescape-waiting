package roomescape.service.exception;

public class ThemeInUseException extends ResourceConflictException {
    public ThemeInUseException(String message) {
        super(message);
    }
}
