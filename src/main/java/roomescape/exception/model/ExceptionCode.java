package roomescape.exception.model;

import org.springframework.http.HttpStatus;

public interface ExceptionCode {

    HttpStatus getHttpStatus();

    String getMessage();
}
