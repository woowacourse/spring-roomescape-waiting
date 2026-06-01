package roomescape.service.exception;

public class ThemeNotFoundException extends ResourceNotFoundException {
    public ThemeNotFoundException(String message) {
        super(message);
    }
}
