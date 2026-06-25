package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final long capacity;
    private final double refillPerSecond;
    private final LongSupplier nanoTime;

    private double tokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSecond) {
        this(capacity, refillPerSecond, System::nanoTime);
    }

    public TokenBucketRateLimiter(
        long capacity,
        double refillPerSecond,
        LongSupplier nanoTime
    ) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity는 0보다 커야 합니다.");
        }
        if (refillPerSecond <= 0) {
            throw new IllegalArgumentException("refillPerSecond는 0보다 커야 합니다.");
        }
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nanoTime = nanoTime;
        this.tokens = capacity;
        this.lastRefillNanos = nanoTime.getAsLong();
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
        double secondsUntilNextToken = (1.0 - tokens) / refillPerSecond;
        return Math.max(1, (long) Math.ceil(secondsUntilNextToken));
    }

    private void refill() {
        long now = nanoTime.getAsLong();
        long elapsedNanos = Math.max(0, now - lastRefillNanos);
        double tokensToAdd = elapsedNanos / NANOS_PER_SECOND * refillPerSecond;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefillNanos = now;
    }
}
