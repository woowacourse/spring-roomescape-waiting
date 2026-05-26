package roomescape.global;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus status();
    String message();
}
