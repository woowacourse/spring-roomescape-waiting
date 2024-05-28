package roomescape.exception;

public class InputNotAllowedException extends BaseException {

    public InputNotAllowedException(String detail) {
        super("입력 형식이 올바르지 않습니다.", detail);
    }
}
