package roomescape.common.ratelimit;

import java.util.function.LongSupplier;

/**
 * 토큰 버킷 방식의 Rate Limiter. 시계는 LongSupplier 로 주입받아 결정적으로 테스트할 수 있다.
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
            return 0;
        }
        return (long) Math.ceil((1 - availableTokens) / refillPerSec);
    }

    private void refill() {
        long now = nanoClock.getAsLong();
        double elapsedSec = (now - lastRefillNanos) / 1_000_000_000.0;
        availableTokens = Math.min(capacity, availableTokens + elapsedSec * refillPerSec);
        lastRefillNanos = now;
    }
}
