package roomescape.global.exception;

public class ForbiddenException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "접근 권한이 없습니다.";

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException() {
        this(DEFAULT_MESSAGE);
    }
}
