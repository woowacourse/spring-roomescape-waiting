package roomescape.global.ratelimit;

public class RateLimitBucket {

    private final int capacity;
    private final double refillPerSecond;
    private final NanoClock nanoClock;

    private double availableTokens;
    private long lastRefillNanoseconds;

    public RateLimitBucket(
            int capacity,
            double refillPerSecond,
            NanoClock nanoClock
    ) {
        if (capacity <= 0) {
            throw new IllegalStateException("토큰의 최대 개수는 0보다 커야 합니다.");
        }
        if (refillPerSecond <= 0) {
            throw new IllegalStateException("토큰의 보충 속도는 0보다 커야 합니다.");
        }

        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nanoClock = nanoClock;

        this.availableTokens = capacity;
        this.lastRefillNanoseconds = nanoClock.currentNanoseconds();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (availableTokens >= 1) {
            availableTokens -= 1;
            return true;
        }

        return false;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        if (availableTokens >= 1) {
            return 0;
        }

        double requiredTokenAmount = 1 - availableTokens;

        return (long) Math.ceil(requiredTokenAmount / refillPerSecond);
    }

    private void refill() {
        long now = nanoClock.currentNanoseconds();
        double elapsedSeconds = parseNanosecondsToSeconds(now - lastRefillNanoseconds);
        double refilledTokens = elapsedSeconds * refillPerSecond;

        availableTokens = Math.min(capacity, availableTokens + refilledTokens);
        lastRefillNanoseconds = now;
    }

    private double parseNanosecondsToSeconds(long nanoseconds) {
        return nanoseconds / 1_000_000_000.0;
    }
}
