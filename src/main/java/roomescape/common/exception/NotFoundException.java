package roomescape.common.exception;

public class NotFoundException extends CustomException {

    public NotFoundException() {
        super("요청하신 자원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND);
    }

    public NotFoundException(String message) {
        super(message, ErrorCode.NOT_FOUND);
    }

    public NotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
