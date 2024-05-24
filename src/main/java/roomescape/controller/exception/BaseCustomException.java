package roomescape.controller.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseCustomException extends RuntimeException {

    private final HttpStatus status;
    private final String title;
    private final String detail;

    protected BaseCustomException(HttpStatus status, String title, String detail) {
        super(title + " - " + detail);
        this.status = status;
        this.title = title;
        this.detail = detail;
    }

    public HttpStatus getHttpStatus() {
        return this.status;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDetail() {
        return this.detail;
    }
}
