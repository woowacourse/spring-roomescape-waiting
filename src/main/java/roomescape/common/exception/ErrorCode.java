package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String getMessage();
    HttpStatus getHttpStatus();
}
