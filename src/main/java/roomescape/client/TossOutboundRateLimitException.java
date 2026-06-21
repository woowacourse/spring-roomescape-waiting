package roomescape.client;

/**
 * 나가는 호출이 자체 Rate Limit 을 초과해, 외부(토스)로 보내지 않고 거부한 경우.
 */
public class TossOutboundRateLimitException extends RuntimeException {

    public TossOutboundRateLimitException() {
        super("결제 승인 요청이 많아 잠시 후 다시 시도해주세요.");
    }
}
