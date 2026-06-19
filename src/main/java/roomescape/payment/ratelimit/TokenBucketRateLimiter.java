package roomescape.payment.ratelimit;

import java.util.function.LongSupplier;

/**
 * 토큰 버킷(Token Bucket) 방식의 Rate Limiter.
 * 시계를 {@link LongSupplier} 로 주입받아, 테스트에서 가짜 시계로 결정적으로 검증할 수 있다.
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

    public synchronized boolean tryConsume() {
        refill();
        if (availableTokens >= 1) {
            availableTokens -= 1;
            return true;
        }
        return false;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        if (availableTokens >= 1) {
            return 0L;
        }
        return (long) Math.ceil((1 - availableTokens) / refillPerSec);
    }

    private void refill() {
        var now = nanoClock.getAsLong();
        var elapsedSec = (now - lastRefillNanos) / 1_000_000_000.0;
        availableTokens = Math.min(capacity, availableTokens + elapsedSec * refillPerSec);
        lastRefillNanos = now;
    }
}
