package roomescape.infrastructure.ratelimiter;

import java.util.function.LongSupplier;

public class TokenBucket {
    private final double capacity;
    private final double refillPerSec;
    private final LongSupplier nanoTimeSupplier;

    private double tokens;
    private long lastRefillNanos;

    public TokenBucket(double capacity, double refillPerSec, LongSupplier nanoTimeSupplier) {
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoTimeSupplier = nanoTimeSupplier;
        this.tokens = capacity;
        this.lastRefillNanos = nanoTimeSupplier.getAsLong();
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
        if (tokens >= 1.0) {
            return 0;
        }
        double tokensNeeded = 1.0 - tokens;
        double secondsNeeded = tokensNeeded / refillPerSec;
        return (long) Math.ceil(secondsNeeded);
    }

    private void refill() {
        long now = nanoTimeSupplier.getAsLong();
        double elapsedSeconds = (now - lastRefillNanos) / 1_000_000_000.0;
        
        if (elapsedSeconds > 0) {
            tokens = Math.min(capacity, tokens + (elapsedSeconds * refillPerSec));
            lastRefillNanos = now;
        }
    }
}
