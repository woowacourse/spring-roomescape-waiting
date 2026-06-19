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
        // TODO: refill() 후 토큰이 1개 이상이면 1개 소비하고 true, 아니면 false.
        refill();

        if (availableTokens >= 1) {
            availableTokens--;
            return true;
        }

        return false;
    }

    /**
     * 다음 요청이 통과 가능해질 때까지 권장 대기 시간(초). 토큰이 충분하면 0.
     */
    public synchronized long retryAfterSeconds() {
        // TODO: refill() 후 토큰이 1개 이상이면 0, 부족하면 1개가 찰 때까지 필요한 초를 올림으로 반환한다.
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
        // TODO: 경과 시간(nanoClock - lastRefillNanos)에 비례해 토큰을 보충하고(상한 capacity), lastRefillNanos 를 갱신한다.
        long now = nanoClock.getAsLong();
        double elapsedSec = (now - lastRefillNanos) / 1_000_000_000.0;
        availableTokens = Math.min(capacity, availableTokens + elapsedSec * refillPerSec);
        lastRefillNanos = now;
    }

}
