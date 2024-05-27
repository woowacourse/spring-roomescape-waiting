package roomescape.exception;

public class UnAuthorizedException extends RuntimeException {
    public UnAuthorizedException() {
        super("권한이 없습니다.");
    }
}
