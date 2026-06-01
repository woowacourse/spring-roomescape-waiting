package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorPolicy {
    String code();
    String message();
    HttpStatus status();
}
