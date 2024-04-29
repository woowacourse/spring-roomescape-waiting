package roomescape.controller.exception;

public abstract class BaseException extends RuntimeException {

    private final String title;
    private final String detail;

    public BaseException(String title, String detail) {
        super(title + " - " + detail);
        this.title = title;
        this.detail = detail;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }
}
