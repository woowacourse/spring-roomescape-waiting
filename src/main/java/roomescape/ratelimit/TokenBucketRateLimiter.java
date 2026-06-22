package roomescape.ratelimit;

import java.util.function.LongSupplier;

/**
 * 토큰 버킷(Token Bucket) 방식의 Rate Limiter.
 *
 * <p>capacity(허용 버스트)만큼 토큰이 차 있고, 매초 refillPerSec(평균 TPS 상한)개씩 보충된다.
 * 요청은 토큰 1개를 소비하며, 없으면 거부된다. 들어오는/나가는 호출에 같은 알고리즘으로 재사용한다.
 *
 * <p>시계를 {@link LongSupplier} 로 주입받아, 테스트에서 가짜 시계로 결정적으로 검증할 수 있다.
 */
public class TokenBucketRateLimiter {

    private static final double MINIMUM_TOKEN = 1.0;
    private static final long NANO_TO_SECOND = 1_000_000_000L;

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
        if (!hasAvailableToken()) {
            return false;
        }
        availableTokens -= MINIMUM_TOKEN;
        return true;
    }

    /**
     * 다음 요청이 통과 가능해질 때까지 권장 대기 시간(초). 토큰이 충분하면 0,
     * 부족하면 1개가 찰 때까지 필요한 초를 올림(Math.ceil)으로 반환한다.
     */
    public synchronized long retryAfterSeconds() {
        refill();
        if (hasAvailableToken()) {
            return 0;
        }
        double tokensNeeded = MINIMUM_TOKEN - availableTokens;
        return (long) Math.ceil(tokensNeeded / refillPerSec);
    }

    /**
     * 마지막 보충 이후 경과 시간에 비례해 토큰을 보충한다(상한 capacity).
     */
    private void refill() {
        long nanoTime = nanoClock.getAsLong();
        double elapsedSeconds = (nanoTime - lastRefillNanos) / (double) NANO_TO_SECOND;
        availableTokens = Math.min(capacity, availableTokens + refillPerSec * elapsedSeconds);
        lastRefillNanos = nanoTime;
    }

    private boolean hasAvailableToken() {
        return availableTokens >= MINIMUM_TOKEN;
    }
}
