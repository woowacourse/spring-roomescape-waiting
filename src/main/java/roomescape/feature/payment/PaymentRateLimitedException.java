package roomescape.feature.payment;

/**
 * 토스가 429(Too Many Requests)를 반복 응답해, Retry-After 기반 재시도를 모두 소진하고도
 * 결제 승인을 받지 못했을 때의 도메인 실패.
 *
 * {@link PaymentClientException}(전송 실패, 재시도 대상)과는 별개 계열이다.
 * 인터셉터가 이미 자체 재시도를 소진했으므로, {@code @Retryable} 이 다시 재시도하지 않도록
 * 의도적으로 {@link PaymentClientException} 을 상속하지 않는다.
 *
 * {@code retryAfterSeconds} 는 마지막 429가 알려준 재시도 권장 시간으로, 클라이언트에 503 + Retry-After 로 전달된다.
 */
public class PaymentRateLimitedException extends RuntimeException {

    private final long retryAfterSeconds;

    public PaymentRateLimitedException(int maxAttempts, long retryAfterSeconds) {
        super("토스가 요청 한도(429)를 반복 반환해 %d회 시도 후에도 결제를 처리하지 못했습니다.".formatted(maxAttempts));
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
