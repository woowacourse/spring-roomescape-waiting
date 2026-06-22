package roomescape.payment.exception;

/**
 * 나가는 토스 호출이 자체 Rate Limit(아웃바운드 토큰 버킷)을 넘겨, 외부로 보내지 않고 거부됐음을 나타낸다.
 * 결제가 실패한 게 아니라 '지금은 보내지 않음'이므로, 잠시 후 재시도 가능한 일시적 상태로 다룬다.
 */
public class OutboundRateLimitException extends RuntimeException {
    public OutboundRateLimitException(String message) {
        super(message);
    }
}
