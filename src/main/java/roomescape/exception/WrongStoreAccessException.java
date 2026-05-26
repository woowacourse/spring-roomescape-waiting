package roomescape.exception;

public class WrongStoreAccessException extends BusinessException {

    public WrongStoreAccessException() {
        super(ErrorType.WRONG_STORE_ACCESS);
    }
}
