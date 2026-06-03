package roomescape.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String getCode();

    HttpStatus getHttpStatus();

    String getMessage();
}
