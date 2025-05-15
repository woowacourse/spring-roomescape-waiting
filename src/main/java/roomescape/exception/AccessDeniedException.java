package roomescape.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends CustomException {

    private static final String MESSAGE = "이 작업을 수행할 권한이 없습니다.";
    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;

    public AccessDeniedException() {
        super(MESSAGE, STATUS);
    }
}
