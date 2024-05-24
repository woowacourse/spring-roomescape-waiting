package roomescape.exception;

public class UnauthorizedException extends IllegalArgumentException {
    public UnauthorizedException() {
        super("권한이 없습니다.");
    }
}
