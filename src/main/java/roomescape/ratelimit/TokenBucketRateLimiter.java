package roomescape.ratelimit;

import java.util.function.LongSupplier;

/**
 * 토큰 버킷(Token Bucket) 방식의 Rate Limiter.
 *
 * <p>시계를 {@link LongSupplier} 로 주입받아, 테스트에서 가짜 시계로 결정적으로 검증할 수 있다.
 */
public class TokenBucketRateLimiter {

    private final long capacity;        // 버킷 최대 용량(버스트 허용량)
    private final double refillPerSec;    // 초당 보충 토큰 수 = 허용 TPS 상한
    private final LongSupplier nanoClock;   // 시계 — 실제론 System::nanoTime, 테스트에선 AtomicLong

    private double availableTokens;   // 남은(사용가능한) 토큰
    private long lastRefillNanos;   // 마지막으로 물을 채운 시각(나노초)

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoClock) {
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoClock = nanoClock;
        this.availableTokens = capacity;            // 시작은 가득 찬 상태
        this.lastRefillNanos = nanoClock.getAsLong();
    }

    /**
     * 토큰이 있으면 1개 소비하고 true, 없으면 false. 요청 하나 처리 시도
     */
    public synchronized boolean tryConsume() {
        // refill() 후 토큰이 1개 이상이면 1개 소비하고 true, 아니면 false.
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
        // refill() 후 토큰이 1개 이상이면 0, 부족하면 1개가 찰 때까지 필요한 초를 올림으로 반환한다.
        refill();
        if (availableTokens >= 1) {
            return 0;
        }
        double needed = 1.0 - availableTokens;
        return (long) Math.ceil(needed / refillPerSec);
    }

    /**
     * 마지막 보충 이후 경과 시간에 비례해 토큰을 보충한다(상한 capacity).
     */
    private void refill() {
        // 경과 시간(nanoClock - lastRefillNanos)에 비례해 토큰을 보충하고(상한 capacity), lastRefillNanos 를 갱신한다.
        long now = nanoClock.getAsLong();    // 현재 시각(나노초)
        double elapsed = (now - lastRefillNanos) / 1000000000.0;     // 경과시간(나노초를 초로 변환)

        availableTokens = Math.min(capacity, availableTokens + elapsed * refillPerSec);  // 크기를 넘으면 버린다.
        lastRefillNanos = now;
    }

}