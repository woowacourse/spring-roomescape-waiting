package roomescape.common.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final int capacity;
    private final int refillPerSec;
    private final LongSupplier nanoTimeSupplier;

    private double tokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(
            final int capacity,
            final int refillPerSec,
            final LongSupplier nanoTimeSupplier
    ) {
        validate(capacity, refillPerSec, nanoTimeSupplier);
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoTimeSupplier = nanoTimeSupplier;
        this.tokens = capacity;
        this.lastRefillNanos = nanoTimeSupplier.getAsLong();
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

        return (long) Math.ceil((1 - tokens) / refillPerSec);
    }

    private void refill() {
        final long currentNanos = nanoTimeSupplier.getAsLong();
        final long elapsedNanos = currentNanos - lastRefillNanos;
        if (elapsedNanos <= 0) {
            return;
        }

        final double refillTokens = (double) elapsedNanos * refillPerSec / NANOS_PER_SECOND;
        tokens = Math.min(capacity, tokens + refillTokens);
        lastRefillNanos = currentNanos;
    }

    private void validate(
            final int capacity,
            final int refillPerSec,
            final LongSupplier nanoTimeSupplier
    ) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("토큰 버킷 capacity는 1 이상이어야 합니다.");
        }
        if (refillPerSec <= 0) {
            throw new IllegalArgumentException("토큰 버킷 refillPerSec은 1 이상이어야 합니다.");
        }
        if (nanoTimeSupplier == null) {
            throw new IllegalArgumentException("토큰 버킷 시계는 null일 수 없습니다.");
        }
    }
}
