package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ThemeNotFoundException extends RuntimeException {

    public ThemeNotFoundException(Long id) {
        super(id + "번 테마를 찾을 수 없습니다.");
    }
}
