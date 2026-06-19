package roomescape.global.error.type;

import org.springframework.http.HttpStatus;

public interface ErrorType {

    HttpStatus status();

    String message();

    String code();
}
