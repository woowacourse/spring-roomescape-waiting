package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final long capacity;
    private final double refillPerSec;
    private final LongSupplier nanoClock;

    private double tokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoClock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity는 1 이상이어야 합니다: " + capacity);
        }
        if (refillPerSec <= 0) {
            throw new IllegalArgumentException("refillPerSec는 0보다 커야 합니다: " + refillPerSec);
        }
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoClock = nanoClock;
        this.tokens = capacity;
        this.lastRefillNanos = nanoClock.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        if (tokens >= 1.0) {
            return 0;
        }
        double needed = 1.0 - tokens;
        return (long) Math.ceil(needed / refillPerSec);
    }

    private void refill() {
        long now = nanoClock.getAsLong();
        long elapsedNanos = now - lastRefillNanos;
        if (elapsedNanos <= 0) {
            return;
        }
        double refilled = (elapsedNanos / NANOS_PER_SECOND) * refillPerSec;
        tokens = Math.min(capacity, tokens + refilled);
        lastRefillNanos = now;
    }
}
