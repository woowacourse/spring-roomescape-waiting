package roomescape.infra.ratelimit;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

public class TokenBucket {

    private static final long NANOS_PER_SEC = 1_000_000_000L;

    private final int capacity;
    private final double refillPerSec;
    private final LongSupplier clock;
    private final ReentrantLock lock = new ReentrantLock();

    private double tokens;
    private long lastRefillNanos;

    public TokenBucket(int capacity, double refillPerSec, LongSupplier clock) {
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.clock = clock;
        this.tokens = capacity;
        this.lastRefillNanos = clock.getAsLong();
    }

    public static TokenBucket ofRealTime(int capacity, double refillPerSec) {
        return new TokenBucket(capacity, refillPerSec, System::nanoTime);
    }

    public boolean tryConsume() {
        lock.lock();
        try {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public long retryAfterSeconds() {
        lock.lock();
        try {
            refill();
            double deficit = 1.0 - tokens;
            if (deficit <= 0) {
                return 0;
            }
            return (long) Math.ceil(deficit / refillPerSec);
        } finally {
            lock.unlock();
        }
    }

    private void refill() {
        long now = clock.getAsLong();
        double elapsedSecs = (double) (now - lastRefillNanos) / NANOS_PER_SEC;
        tokens = Math.min(capacity, tokens + elapsedSecs * refillPerSec);
        lastRefillNanos = now;
    }
}