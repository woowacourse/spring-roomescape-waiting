package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ThemeNotFoundException extends CustomException {

    private static final String MESSAGE = "테마가 존재하지 않습니다.";
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public ThemeNotFoundException() {
        super(MESSAGE, STATUS);
    }
}
