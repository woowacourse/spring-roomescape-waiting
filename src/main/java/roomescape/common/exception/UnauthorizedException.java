package roomescape.common.exception;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException() {
        super("접근 권한이 없습니다.");
    }
}
