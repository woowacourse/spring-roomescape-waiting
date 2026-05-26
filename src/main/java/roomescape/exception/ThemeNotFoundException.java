package roomescape.exception;

public class ThemeNotFoundException extends BusinessException {

    public ThemeNotFoundException() {
        super(ErrorType.THEME_NOT_FOUND);
    }
}
