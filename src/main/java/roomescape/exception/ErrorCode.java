package roomescape.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getHttpStatus();
    String getErrorMessage();
    String getErrorName();
}
