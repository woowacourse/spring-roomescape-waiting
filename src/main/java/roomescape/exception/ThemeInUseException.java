package roomescape.exception;

public class ThemeInUseException extends BusinessException {

    public ThemeInUseException() {
        super(ErrorType.THEME_IN_USE);
    }
}
