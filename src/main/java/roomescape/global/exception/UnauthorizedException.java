package roomescape.global.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("로그인 해주세요.");
    }
}
