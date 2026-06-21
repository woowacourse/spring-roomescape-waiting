package roomescape.client;

/**
 * 토스가 429(Too Many Requests)를 반복해서 응답해 maxAttempts 까지 재시도해도 승인받지 못한 경우.
 * 429 응답은 아직 처리되지 않은 상태이므로 결제는 확실히 미승인 상태다.
 */
public class TossRateLimitExceededException extends RuntimeException {

    public TossRateLimitExceededException() {
        super("결제 승인 요청이 많아 토스 서버가 일시적으로 거부했습니다. 잠시 후 다시 시도해주세요.");
    }
}
