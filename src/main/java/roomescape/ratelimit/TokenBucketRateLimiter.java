package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final long capacity;
    private final double refillPerSecond;
    private final LongSupplier nanoTimeSupplier;

    private double tokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSecond) {
        this(capacity, refillPerSecond, System::nanoTime);
    }

    public TokenBucketRateLimiter(long capacity, double refillPerSecond, LongSupplier nanoTimeSupplier) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity는 1 이상이어야 합니다.");
        }
        if (refillPerSecond <= 0) {
            throw new IllegalArgumentException("refillPerSecond는 0보다 커야 합니다.");
        }
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nanoTimeSupplier = nanoTimeSupplier;
        this.tokens = capacity;
        this.lastRefillNanos = nanoTimeSupplier.getAsLong();
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
        double missingTokens = 1.0 - tokens;
        return Math.max(1L, (long) Math.ceil(missingTokens / refillPerSecond));
    }

    private void refill() {
        long now = nanoTimeSupplier.getAsLong();
        long elapsedNanos = now - lastRefillNanos;
        if (elapsedNanos <= 0) {
            return;
        }

        double refillTokens = (elapsedNanos / (double) NANOS_PER_SECOND) * refillPerSecond;
        tokens = Math.min(capacity, tokens + refillTokens);
        lastRefillNanos = now;
    }
}
