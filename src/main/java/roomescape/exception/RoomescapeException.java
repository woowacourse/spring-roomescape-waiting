package roomescape.exception;

import org.springframework.http.HttpStatusCode;

public class RoomescapeException extends RuntimeException {

    private final HttpStatusCode httpStatusCode;

    public RoomescapeException(HttpStatusCode httpStatusCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public RoomescapeException(RoomescapeExceptionCode exceptionCode) {
        super(exceptionCode.message());
        this.httpStatusCode = exceptionCode.httpStatusCode();
    }

    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }
}
