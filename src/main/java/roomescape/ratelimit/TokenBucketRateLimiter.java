package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private final long capacity;
    private final double refillPerSec;
    private final LongSupplier nanoClock;

    private double tokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoClock) {
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoClock = nanoClock;
        this.tokens = capacity;
        this.lastRefillNanos = nanoClock.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        if (tokens >= 1) {
            return 0;
        }
        return (long) Math.ceil((1 - tokens) / refillPerSec);
    }

    private void refill() {
        long now = nanoClock.getAsLong();
        double elapsedSec = (now - lastRefillNanos) / 1_000_000_000.0;
        if (elapsedSec <= 0) {
            return;
        }
        tokens = Math.min(capacity, tokens + elapsedSec * refillPerSec);
        lastRefillNanos = now;
    }
}
