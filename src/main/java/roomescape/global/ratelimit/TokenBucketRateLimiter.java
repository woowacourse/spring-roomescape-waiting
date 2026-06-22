package roomescape.global.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private final long capacity;
    private final double refillPerSec;
    private final LongSupplier clock;

    private double tokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier clock) {
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.clock = clock;
        this.tokens = capacity;
        this.lastRefillNanos = clock.getAsLong();
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
        double secondsNeeded = (1.0 - tokens) / refillPerSec;
        return (long) Math.ceil(secondsNeeded);
    }

    private void refill() {
        long now = clock.getAsLong();
        double elapsed = (now - lastRefillNanos) / 1_000_000_000.0;
        tokens = Math.min(capacity, tokens + elapsed * refillPerSec);
        lastRefillNanos = now;
    }
}
