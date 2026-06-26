package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {
    private final long capacity;
    private final double refillPerSecond;
    private final LongSupplier nanoTime;

    private double tokens;
    private long lastRefilledNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSecond, LongSupplier nanoTime) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        if (refillPerSecond <= 0) {
            throw new IllegalArgumentException("refillPerSecond must be positive");
        }
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nanoTime = nanoTime;
        this.tokens = capacity;
        this.lastRefilledNanos = nanoTime.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens < 1) {
            return false;
        }
        tokens -= 1;
        return true;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        if (tokens >= 1) {
            return 0;
        }
        double secondsUntilNextToken = (1 - tokens) / refillPerSecond;
        return (long) Math.ceil(secondsUntilNextToken);
    }

    private void refill() {
        long now = nanoTime.getAsLong();
        long elapsedNanos = now - lastRefilledNanos;
        if (elapsedNanos <= 0) {
            return;
        }
        double refill = elapsedNanos / 1_000_000_000.0 * refillPerSecond;
        tokens = Math.min(capacity, tokens + refill);
        lastRefilledNanos = now;
    }
}
