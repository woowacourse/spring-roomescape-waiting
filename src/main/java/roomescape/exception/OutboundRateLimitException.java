package roomescape.exception;

public class OutboundRateLimitException extends RuntimeException {

    public OutboundRateLimitException() {
        super("나가는 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
    }
}