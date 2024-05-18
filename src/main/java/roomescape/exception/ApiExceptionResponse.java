package roomescape.exception;

import org.springframework.http.HttpStatus;

public record ApiExceptionResponse<T>(HttpStatus status, T message) {
}
