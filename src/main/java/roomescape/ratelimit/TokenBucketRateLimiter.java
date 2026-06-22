package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private static final double TOKEN_EPSILON = 1e-12;
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final long capacity;
    private final double refillPerSec;
    private final LongSupplier nanoTime;

    private double tokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSec) {
        this(capacity, refillPerSec, System::nanoTime);
    }

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoTime) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity는 0보다 커야 합니다.");
        }
        if (!Double.isFinite(refillPerSec) || refillPerSec <= 0) {
            throw new IllegalArgumentException("refillPerSec는 0보다 큰 유한한 값이어야 합니다.");
        }
        if (nanoTime == null) {
            throw new IllegalArgumentException("nanoTime은 null일 수 없습니다.");
        }

        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoTime = nanoTime;
        this.tokens = capacity;
        this.lastRefillNanos = nanoTime.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens + TOKEN_EPSILON < 1.0) {
            return false;
        }
        tokens -= 1.0;
        return true;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        if (tokens + TOKEN_EPSILON >= 1.0) {
            return 0;
        }
        return Math.max(1L, (long) Math.ceil((1.0 - tokens) / refillPerSec));
    }

    private void refill() {
        long now = nanoTime.getAsLong();
        long elapsedNanos = now - lastRefillNanos;
        if (elapsedNanos <= 0) {
            return;
        }

        double refillTokens = elapsedNanos / NANOS_PER_SECOND * refillPerSec;
        tokens = Math.min(capacity, tokens + refillTokens);
        lastRefillNanos = now;
    }
}
