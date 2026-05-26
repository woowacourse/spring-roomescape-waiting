package roomescape.exception;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException() {
        super(ErrorType.AUTHENTICATION_REQUIRED);
    }
}
