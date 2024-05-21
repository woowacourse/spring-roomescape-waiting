package roomescape.exception.theme;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class NotFoundThemeException extends CustomException {
    public NotFoundThemeException() {
        super("존재하지 않는 테마입니다.", HttpStatus.NOT_FOUND);
    }
}
