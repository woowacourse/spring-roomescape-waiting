package roomescape.infra.toss;

public class OutboundRateLimitException extends RuntimeException {

    public OutboundRateLimitException() {
        super("토스 요청 한도를 초과했습니다.");
    }
}
