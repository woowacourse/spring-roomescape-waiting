package roomescape.exception.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        log.warn("비즈니스 규칙 위반 [{}]: {}", status, message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
