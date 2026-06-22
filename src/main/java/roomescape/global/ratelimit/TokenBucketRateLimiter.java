package roomescape.global.ratelimit;

import java.util.function.LongSupplier;

/**
 * 토큰 버킷(Token Bucket) 방식의 Rate Limiter.
 *
 * <p>시계를 {@link LongSupplier}로 주입받아 테스트에서 가짜 시계로 결정적으로 검증할 수 있습니다.
 */
public class TokenBucketRateLimiter {

    private final long capacity;
    private final double refillPerSec;
    private final LongSupplier nanoClock;

    private double availableTokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoClock) {
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoClock = nanoClock;
        this.availableTokens = capacity;
        this.lastRefillNanos = nanoClock.getAsLong();
    }

    /**
     * 토큰이 있으면 1개 소비하고 true, 없으면 false를 반환합니다.
     */
    public synchronized boolean tryConsume() {
        refill();
        if (availableTokens >= 1) {
            availableTokens -= 1;
            return true;
        }
        return false;
    }

    /**
     * 다음 요청이 통과 가능해질 때까지 권장 대기 시간(초)을 반환합니다.
     */
    public synchronized long retryAfterSeconds() {
        refill();
        if (availableTokens >= 1) {
            return 0L;
        }
        return (long) Math.ceil((1 - availableTokens) / refillPerSec);
    }

    /**
     * 마지막 보충 이후 경과 시간에 비례해 토큰을 보충합니다.
     */
    private void refill() {
        long now = nanoClock.getAsLong();
        double elapsedSec = (now - lastRefillNanos) / 1_000_000_000.0;
        availableTokens = Math.min(capacity, availableTokens + elapsedSec * refillPerSec);
        lastRefillNanos = now;
    }
}
