package roomescape.exception.custom;

public class ThemeNotExistsException extends CustomException {

    public ThemeNotExistsException() {
        super("해당 테마가 존재하지 않습니다.");
    }
}
