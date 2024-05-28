package roomescape.exception;

public class OperationNotAllowedException extends BaseException {

    public OperationNotAllowedException(String detail) {
        super("허용되지 않는 작업입니다.", detail);
    }
}
