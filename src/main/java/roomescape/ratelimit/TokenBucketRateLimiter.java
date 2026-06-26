package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final long capacity;
    private final double refillPerSec;
    private final LongSupplier nanoTimeSupplier;

    private double availableTokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoTimeSupplier) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity는 1 이상이어야 합니다. capacity=" + capacity);
        }
        if (refillPerSec <= 0) {
            throw new IllegalArgumentException("refillPerSec는 0보다 커야 합니다. refillPerSec=" + refillPerSec);
        }
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoTimeSupplier = nanoTimeSupplier;
        this.availableTokens = capacity;
        this.lastRefillNanos = nanoTimeSupplier.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (availableTokens >= 1.0) {
            availableTokens -= 1.0;
            return true;
        }
        return false;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        if (availableTokens >= 1.0) {
            return 0L;
        }
        double shortage = 1.0 - availableTokens;
        return (long) Math.ceil(shortage / refillPerSec);
    }

    private void refill() {
        long now = nanoTimeSupplier.getAsLong();
        long elapsedNanos = now - lastRefillNanos;
        if (elapsedNanos <= 0) {
            return;
        }
        double refilled = (elapsedNanos / NANOS_PER_SECOND) * refillPerSec;
        availableTokens = Math.min(capacity, availableTokens + refilled);
        lastRefillNanos = now;
    }
}
