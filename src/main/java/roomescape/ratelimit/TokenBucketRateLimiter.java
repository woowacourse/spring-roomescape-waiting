package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private final double capacity;
    private final double refillPerSec;
    private final LongSupplier clock;

    private double tokens;
    private long lastRefillNano;

    public TokenBucketRateLimiter(double capacity, double refillPerSec, LongSupplier clock) {
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.clock = clock;
        this.tokens = capacity;
        this.lastRefillNano = clock.getAsLong();
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
        double deficit = 1.0 - tokens;
        return (long) Math.ceil(deficit / refillPerSec);
    }

    private void refill() {
        long now = clock.getAsLong();
        double elapsedSeconds = (now - lastRefillNano) / 1_000_000_000.0;
        tokens = Math.min(capacity, tokens + elapsedSeconds * refillPerSec);
        lastRefillNano = now;
    }
}