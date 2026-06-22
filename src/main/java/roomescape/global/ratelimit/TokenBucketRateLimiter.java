package roomescape.global.ratelimit;

import java.util.Objects;
import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final long capacity;
    private final double refillPerSec;
    private final LongSupplier nanoClock;

    private double availableTokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoClock) {
        validate(capacity, refillPerSec);
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoClock = Objects.requireNonNull(nanoClock);
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
        if (now <= lastRefillNanos) {
            return;
        }

        double elapsedSeconds = (now - lastRefillNanos) / NANOS_PER_SECOND;
        availableTokens = Math.min(capacity, availableTokens + elapsedSeconds * refillPerSec);
        lastRefillNanos = now;
    }

    private void validate(long capacity, double refillPerSec) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity는 1 이상이어야 합니다.");
        }
        if (Double.isNaN(refillPerSec) || Double.isInfinite(refillPerSec) || refillPerSec <= 0) {
            throw new IllegalArgumentException("refillPerSec는 0보다 큰 유한한 값이어야 합니다.");
        }
    }
}
