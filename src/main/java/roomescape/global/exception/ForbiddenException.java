package roomescape.global.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("접근 권한이 부족합니다.");
    }
}
