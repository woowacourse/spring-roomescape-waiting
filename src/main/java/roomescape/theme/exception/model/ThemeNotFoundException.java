package roomescape.theme.exception.model;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.model.RoomEscapeException;

public class ThemeNotFoundException extends RoomEscapeException {

    private static final String THEME_NOT_EXIST_MESSAGE = "해당하는 테마가 존재하지 않습니다.";

    public ThemeNotFoundException() {
        super(THEME_NOT_EXIST_MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
