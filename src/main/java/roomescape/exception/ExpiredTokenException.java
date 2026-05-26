package roomescape.exception;

public class ExpiredTokenException extends BusinessException {

    public ExpiredTokenException() {
        super(ErrorType.EXPIRED_TOKEN);
    }
}
