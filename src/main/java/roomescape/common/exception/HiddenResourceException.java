package roomescape.common.exception;

public class HiddenResourceException extends DomainException {
    public HiddenResourceException() {
        super("존재하지 않는 리소스입니다.");
    }
}
