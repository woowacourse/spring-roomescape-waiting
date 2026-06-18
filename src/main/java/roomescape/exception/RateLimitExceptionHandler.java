package roomescape.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.ratelimit.OutboundRateLimitException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RateLimitExceptionHandler {

    @ExceptionHandler(OutboundRateLimitException.class)
    public ResponseEntity<ErrorResponse> handle(OutboundRateLimitException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()))
                .body(new ErrorResponse("OUTBOUND_RATE_LIMITED", e.getMessage()));
    }
}
