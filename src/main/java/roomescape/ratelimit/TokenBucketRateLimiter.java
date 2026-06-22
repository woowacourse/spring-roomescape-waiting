package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final long capacity;
    private final double refillPerSecond;
    private final LongSupplier nanoClock;

    private double availableTokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSecond, LongSupplier nanoClock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity는 0보다 커야 합니다.");
        }
        if (refillPerSecond <= 0) {
            throw new IllegalArgumentException("refillPerSecond는 0보다 커야 합니다.");
        }

        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nanoClock = nanoClock;
        this.availableTokens = capacity;
        this.lastRefillNanos = nanoClock.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (availableTokens < 1) {
            return false;
        }

        availableTokens -= 1;
        return true;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        if (availableTokens >= 1) {
            return 0;
        }

        return (long) Math.ceil((1 - availableTokens) / refillPerSecond);
    }

    private void refill() {
        long now = nanoClock.getAsLong();
        double elapsedSeconds = (now - lastRefillNanos) / NANOS_PER_SECOND;
        availableTokens = Math.min(capacity, availableTokens + elapsedSeconds * refillPerSecond);
        lastRefillNanos = now;
    }
}
