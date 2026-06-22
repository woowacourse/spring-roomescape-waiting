package roomescape.client;

/**
 * 나가는(egress) 호출이 자체 한도를 넘어, 외부로 보내지 않고 거부했음을 알리는 예외.
 * 한도를 넘겨 호출하면 어차피 상대가 429로 거부하니, 보내기 전에 스스로 조절한다.
 */
public class OutboundRateLimitException extends RuntimeException {

    public OutboundRateLimitException(String message) {
        super(message);
    }
}
