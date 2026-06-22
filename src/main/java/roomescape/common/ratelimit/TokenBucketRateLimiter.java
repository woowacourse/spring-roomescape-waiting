package roomescape.common.ratelimit;

import java.util.function.LongSupplier;

/**
 * 토큰 버킷 Rate Limiter. capacity만큼 토큰이 차 있고 매초 refillPerSec개씩 보충된다. 요청은 토큰 1개를
 * 소비하고(tryConsume), 없으면 거부된다. 들어오는/나가는 두 방향에서 같은 알고리즘으로 재사용한다(방향만 다름).
 *
 * <p>보충은 폴링이 아니라 '마지막 보충 이후 경과 시간 × refillPerSec'로 그때그때 계산하며(lazy refill),
 * capacity를 넘지 않는다. 시간은 System.nanoTime을 직접 박지 않고 LongSupplier로 주입받아, 가짜 시계로
 * 결정적 테스트가 가능하다. 동시 요청에서도 정확히 capacity개만 통과하도록 상태 변경을 synchronized로 직렬화한다.
 */
public class TokenBucketRateLimiter {

    private static final double ONE_TOKEN = 1.0;
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final long capacity;
    private final double refillPerSec;
    private final LongSupplier nanoClock;

    private double tokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoClock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("버킷 용량은 1 이상이어야 합니다.");
        }
        if (refillPerSec <= 0) {
            throw new IllegalArgumentException("초당 보충량은 0보다 커야 합니다.");
        }
        if (nanoClock == null) {
            throw new IllegalArgumentException("시계는 비어 있을 수 없습니다.");
        }
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoClock = nanoClock;
        this.tokens = capacity; // 시작은 가득 — 초기 버스트(capacity)까지 허용한다.
        this.lastRefillNanos = nanoClock.getAsLong();
    }

    /** 토큰이 1개 이상이면 1개 소비하고 통과(true), 없으면 거부(false). */
    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= ONE_TOKEN) {
            tokens -= ONE_TOKEN;
            return true;
        }
        return false;
    }

    /** 토큰 1개가 찰 때까지 필요한 초를 올림(ceil)으로 반환한다. 이미 1개 이상이면 0. */
    public synchronized long retryAfterSeconds() {
        refill();
        if (tokens >= ONE_TOKEN) {
            return 0L;
        }
        return (long) Math.ceil((ONE_TOKEN - tokens) / refillPerSec);
    }

    private void refill() {
        long now = nanoClock.getAsLong();
        long elapsedNanos = now - lastRefillNanos;
        if (elapsedNanos <= 0) {
            return; // 시계가 안 흘렀으면 보충 없음(같은 틱의 동시 호출 등).
        }
        double refilled = (elapsedNanos / NANOS_PER_SECOND) * refillPerSec;
        tokens = Math.min((double) capacity, tokens + refilled);
        lastRefillNanos = now;
    }
}
