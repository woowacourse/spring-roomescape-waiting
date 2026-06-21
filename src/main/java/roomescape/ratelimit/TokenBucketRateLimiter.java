package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private final long capacity;
    private final double refillPerSecond;
    private final LongSupplier nanoClock;

    private double availableTokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSecond, LongSupplier nanoClock) {
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
        double elapsedSeconds = (now - lastRefillNanos) / 1_000_000_000.0;
        availableTokens = Math.min(capacity, availableTokens + elapsedSeconds * refillPerSecond);
        lastRefillNanos = now;
    }
}
