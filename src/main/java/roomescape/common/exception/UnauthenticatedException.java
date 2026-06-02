package roomescape.common.exception;

public class UnauthenticatedException extends DomainException {
    public UnauthenticatedException() {
        super("인증이 필요합니다.");
    }
}
