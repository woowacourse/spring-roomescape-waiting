package roomescape.common.ratelimit;

import java.util.function.LongSupplier;

/**
 * 토큰 버킷 기반 Rate Limiter. capacity만큼 토큰이 차 있고 매초 refillPerSec개씩 보충된다.
 * 시간 의존 로직은 LongSupplier(나노초)로 주입받아 가짜 시계로 결정적으로 테스트할 수 있다.
 */
public class TokenBucketRateLimiter {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final long capacity;
    private final double refillPerSec;
    private final LongSupplier nanoTimeSupplier;

    private double tokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoTimeSupplier) {
        if (capacity <= 0 || refillPerSec <= 0) {
            throw new IllegalArgumentException("capacity와 refillPerSec는 0보다 커야 합니다.");
        }
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoTimeSupplier = nanoTimeSupplier;
        this.tokens = capacity;
        this.lastRefillNanos = nanoTimeSupplier.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        if (tokens >= 1) {
            return 0;
        }
        return (long) Math.ceil((1 - tokens) / refillPerSec);
    }

    private void refill() {
        long now = nanoTimeSupplier.getAsLong();
        double elapsedSeconds = (now - lastRefillNanos) / (double) NANOS_PER_SECOND;
        if (elapsedSeconds > 0) {
            tokens = Math.min(capacity, tokens + elapsedSeconds * refillPerSec);
            lastRefillNanos = now;
        }
    }
}
