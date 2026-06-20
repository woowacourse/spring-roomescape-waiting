package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final long capacity;
    private final double refillPerSecond;
    private final LongSupplier nanoTime;

    private double tokens;
    private long lastRefilledAt;

    public TokenBucketRateLimiter(long capacity, double refillPerSecond, LongSupplier nanoTime) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nanoTime = nanoTime;
        this.tokens = capacity;
        this.lastRefilledAt = nanoTime.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens < 1.0) {
            return false;
        }
        tokens -= 1.0;
        return true;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        if (tokens >= 1.0) {
            return 0;
        }
        if (refillPerSecond <= 0.0) {
            return 1;
        }
        double requiredTokens = 1.0 - tokens;
        return Math.max(1L, (long) Math.ceil(requiredTokens / refillPerSecond));
    }

    private void refill() {
        long now = nanoTime.getAsLong();
        long elapsedNanos = Math.max(0L, now - lastRefilledAt);
        if (elapsedNanos == 0L || refillPerSecond <= 0.0) {
            lastRefilledAt = now;
            return;
        }
        double refillTokens = elapsedNanos * refillPerSecond / NANOS_PER_SECOND;
        tokens = Math.min(capacity, tokens + refillTokens);
        lastRefilledAt = now;
    }
}
