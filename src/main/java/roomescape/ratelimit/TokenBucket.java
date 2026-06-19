package roomescape.ratelimit;

import java.util.function.LongSupplier;

public class TokenBucket {

    private final long capacity;
    private final double refillPerSecond;
    private final LongSupplier nanoTime;
    private double tokens;
    private long lastRefillNanos;

    public TokenBucket(long capacity, double refillPerSecond, LongSupplier nanoTime) {
        if (capacity <= 0 || refillPerSecond <= 0) {
            throw new IllegalArgumentException("Token bucket settings must be positive");
        }
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nanoTime = nanoTime;
        this.tokens = capacity;
        this.lastRefillNanos = nanoTime.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens < 1D) {
            return false;
        }
        tokens--;
        return true;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        if (tokens >= 1D) {
            return 0L;
        }
        return (long) Math.ceil((1D - tokens) / refillPerSecond);
    }

    private void refill() {
        long nowNanos = nanoTime.getAsLong();
        double elapsedSeconds = (nowNanos - lastRefillNanos) / 1_000_000_000D;
        tokens = Math.min(capacity, tokens + elapsedSeconds * refillPerSecond);
        lastRefillNanos = nowNanos;
    }
}
