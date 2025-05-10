package roomescape.global.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("로그인되지 않은 상태이다.");
    }
}
