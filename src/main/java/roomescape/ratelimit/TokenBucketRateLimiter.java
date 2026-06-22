package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final long capacity;
    private final long refillPerSecond;
    private final LongSupplier nanoTimeSupplier;
    private double tokens;
    private long lastRefilledAt;

    public TokenBucketRateLimiter(long capacity, long refillPerSecond) {
        this(capacity, refillPerSecond, System::nanoTime);
    }

    public TokenBucketRateLimiter(long capacity, long refillPerSecond, LongSupplier nanoTimeSupplier) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity는 1 이상이어야 합니다.");
        }
        if (refillPerSecond < 1) {
            throw new IllegalArgumentException("refillPerSecond는 1 이상이어야 합니다.");
        }
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nanoTimeSupplier = nanoTimeSupplier;
        this.tokens = capacity;
        this.lastRefilledAt = nanoTimeSupplier.getAsLong();
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
        double missingTokens = 1 - tokens;
        return Math.max(1, (long) Math.ceil(missingTokens / refillPerSecond));
    }

    private void refill() {
        long now = nanoTimeSupplier.getAsLong();
        long elapsedNanos = now - lastRefilledAt;
        if (elapsedNanos <= 0) {
            return;
        }
        double tokensToAdd = (elapsedNanos / (double) NANOS_PER_SECOND) * refillPerSecond;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefilledAt = now;
    }
}
