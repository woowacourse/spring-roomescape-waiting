package roomescape.global.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ErrorResponseBuilder {
    private HttpStatus httpStatus;
    private String errorMessage;
    private LocalDateTime timeStamp;
    private String apiUrl;
    private String traceId;

    public ErrorResponseBuilder httpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public ErrorResponseBuilder errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public ErrorResponseBuilder timeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public ErrorResponseBuilder apiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return this;
    }

    public ErrorResponseBuilder traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public ErrorResponse build() {
        return new ErrorResponse(httpStatus, errorMessage, timeStamp, apiUrl, traceId);
    }
}
