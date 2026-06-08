package roomescape.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 값의 형식이 올바르지 않습니다."),
    INVALID_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "요청 파라미터 형식이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류");

    private final HttpStatus status;
    private final String message;
}
