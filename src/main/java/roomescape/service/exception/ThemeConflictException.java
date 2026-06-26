package roomescape.service.exception;

public class ThemeConflictException extends ResourceConflictException {
    public ThemeConflictException(String message) {
        super(message);
    }
}
