package roomescape.infrastructure.ratelimit;

import java.util.function.LongSupplier;

/**
 * 토큰 버킷(Token Bucket) 방식의 Rate Limiter.
 *
 * <p>시계를 {@link LongSupplier} 로 주입받아, 테스트에서 가짜 시계로 결정적으로 검증할 수 있다.
 */
public class TokenBucketRateLimiter {

    private final long capacity;        // 버킷 최대 용량(버스트 허용량)
    private final double refillPerSec;  // 초당 보충 토큰 수 = 허용 TPS 상한
    private final LongSupplier nanoClock;

    private double availableTokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoClock) {
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoClock = nanoClock;
        this.availableTokens = capacity;            // 시작은 가득 찬 상태
        this.lastRefillNanos = nanoClock.getAsLong();
    }

    /**
     * 토큰이 있으면 1개 소비하고 true, 없으면 false.
     */
    public synchronized boolean tryConsume() {
        refill();
        if (availableTokens >= 1) {
            availableTokens -= 1;
            return true;
        }
        return false;
    }

    /**
     * 다음 요청이 통과 가능해질 때까지 권장 대기 시간(초). 토큰이 충분하면 0.
     */
    public synchronized long retryAfterSeconds() {
        refill();
        if (availableTokens >= 1) {
            return 0;
        }
        return (long) Math.ceil((1 - availableTokens) / refillPerSec);
    }

    /**
     * 마지막 보충 이후 경과 시간에 비례해 토큰을 보충한다(상한 capacity).
     */
    private void refill() {
        var now = nanoClock.getAsLong();
        var elapsedSec = (now - lastRefillNanos) / 1_000_000_000.0;
        availableTokens = Math.min(capacity, availableTokens + elapsedSec * refillPerSec);
        lastRefillNanos = now;
    }

}
