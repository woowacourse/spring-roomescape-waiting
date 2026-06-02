package roomescape.global.exception;

public class InvalidRequestValueException extends BusinessException {

    public InvalidRequestValueException(String message) {
        super(message);
    }

    public InvalidRequestValueException() {
        this("값이 유효하지 않습니다.");
    }
}
