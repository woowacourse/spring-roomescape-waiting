package roomescape.exception.business;

import org.springframework.http.HttpStatus;

public class AdminAccessException extends BusinessException {

    public AdminAccessException() {
        super(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
    }
}
