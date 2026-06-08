package roomescape.theme.exception;

import org.springframework.http.HttpStatus;

import roomescape.global.error.BusinessException;
import roomescape.global.error.ErrorCode;

public class ThemeNotFoundException extends BusinessException {
    public ThemeNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, ErrorCode.THEME_NOT_FOUND, "테마를 찾을 수 없습니다. id=" + id);
    }
}
