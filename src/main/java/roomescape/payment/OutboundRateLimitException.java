package roomescape.payment;

/**
 * 나가는 토스 호출이 자체 Rate Limit 을 넘어, 외부로 보내지 않고 거부했음을 알리는 도메인 예외.
 *
 * <p>한도를 넘겨 호출하면 어차피 토스가 429 로 거부하니, 보내기 전에 스스로 조절한다(요청은 토스에 닿지 않는다).
 */
public class OutboundRateLimitException extends RuntimeException {

    public OutboundRateLimitException(String message) {
        super(message);
    }
}
