package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private final long capacity;
    private final double refillPerSec;
    private final LongSupplier nanoClock;

    private double availableTokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoClock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity는 0보다 커야 합니다.");
        }
        if (refillPerSec <= 0) {
            throw new IllegalArgumentException("refillPerSec는 0보다 커야 합니다.");
        }
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
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
        return (long) Math.ceil((1 - availableTokens) / refillPerSec);
    }

    private void refill() {
        long now = nanoClock.getAsLong();
        long elapsedNanos = now - lastRefillNanos;
        if (elapsedNanos <= 0) {
            return;
        }

        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;
        availableTokens = Math.min(capacity, availableTokens + elapsedSeconds * refillPerSec);
        lastRefillNanos = now;
    }
}
