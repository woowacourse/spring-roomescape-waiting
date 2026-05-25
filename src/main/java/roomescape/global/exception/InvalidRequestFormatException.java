package roomescape.global.exception;

public class InvalidRequestFormatException extends BusinessException {

    public InvalidRequestFormatException() {
        super("요청 본문 형식이 유효하지 않습니다.");
    }
}
