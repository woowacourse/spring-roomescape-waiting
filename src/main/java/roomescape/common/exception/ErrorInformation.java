package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorInformation {
    HttpStatus getHttpStatus();
    String getErrorCode();
    String getMessage();
}
