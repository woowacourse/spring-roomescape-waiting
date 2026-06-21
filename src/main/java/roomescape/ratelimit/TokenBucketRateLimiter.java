package roomescape.ratelimit;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

public class TokenBucketRateLimiter {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final long capacity;
    private final double refillPerSec;
    private final LongSupplier clock;
    private final AtomicReference<State> stateRef;

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier clock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity는 양수여야 합니다: " + capacity);
        }
        if (refillPerSec <= 0) {
            throw new IllegalArgumentException("refillPerSec는 양수여야 합니다: " + refillPerSec);
        }
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.clock = clock;
        this.stateRef = new AtomicReference<>(new State(capacity, clock.getAsLong()));
    }

    public boolean tryConsume() {
        while (true) {
            State current = stateRef.get();
            long nowNanos = clock.getAsLong();
            State refilled = refill(current, nowNanos);

            if (refilled.tokens < 1.0) {
                return false;
            }

            State next = new State(refilled.tokens - 1.0, refilled.lastRefillNanos);
            if (stateRef.compareAndSet(current, next)) {
                return true;
            }
        }
    }

    public long retryAfterSeconds() {
        State currentState = stateRef.get();
        long nowNanos = clock.getAsLong();
        State refilled = refill(currentState, nowNanos);

        if (refilled.tokens >= 1.0) {
            return 0L;
        }

        double tokensNeeded = 1.0 - refilled.tokens;
        double secondsNeeded = tokensNeeded / refillPerSec;
        return (long) Math.ceil(secondsNeeded);
    }

    private State refill(State current, long nowNanos) {
        long elapsedNanos = nowNanos - current.lastRefillNanos;
        if (elapsedNanos <= 0L) {
            return current;
        }
        double tokensToAdd = (double) elapsedNanos / NANOS_PER_SECOND * refillPerSec;
        double newTokens = Math.min(capacity, current.tokens + tokensToAdd);
        return new State(newTokens, nowNanos);
    }

    private record State(double tokens, long lastRefillNanos) {
    }
}
