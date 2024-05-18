package roomescape.exception.theme;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class InvalidThemeNameLengthException extends RoomescapeException {
    public InvalidThemeNameLengthException() {
        super("유효하지 않은 테마 이름 길이입니다. 이름은 16자 이하여야 합니다.", HttpStatus.BAD_REQUEST);
    }
}
