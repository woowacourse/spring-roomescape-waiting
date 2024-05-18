package roomescape.exception;

import org.springframework.http.HttpStatus;

public record ApiExceptionResponse(HttpStatus status, String message) {
}
