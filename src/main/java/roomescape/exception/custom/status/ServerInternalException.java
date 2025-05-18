package roomescape.exception.custom.status;

public class ServerInternalException extends RuntimeException {

    public ServerInternalException() {
        super("서버 내부 오류가 발생했습니다.");
    }
}
