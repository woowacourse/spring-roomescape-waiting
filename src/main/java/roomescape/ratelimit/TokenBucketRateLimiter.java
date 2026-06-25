package roomescape.ratelimit;

import java.util.function.LongSupplier;

// 토큰 버킷 알고리즘 방식의 Rate Limiter.
public class TokenBucketRateLimiter {

  private final long capacity; // 버킷 최대 용량
  private final double refillPerSec; // 초당 보충 토큰 수 = 허용 TPS 상한
  private final LongSupplier nanoClock; // 시간 의존 로직

  private double availableTokens;
  private long lastRefillNanos;

  public TokenBucketRateLimiter(long capacity, double refillPerSec, LongSupplier nanoClock) {
    this.capacity = capacity;
    this.refillPerSec = refillPerSec;
    this.nanoClock = nanoClock;
    this.availableTokens = capacity; // 시작 토큰량은 가득 찬 상태
    this.lastRefillNanos = nanoClock.getAsLong();
  }

  // 토큰이 있으면 1개 소비하고 true(통과), 없으면 false(거부)
  public synchronized boolean tryConsume() {
    refill();
    // 아래 check & act 가 synchronized 이므로 원자적으로 실행된다.
    if (availableTokens >= 1) {
      availableTokens -= 1;
      return true;
    }

    return false;
  }

  // 1개가 찰 때까지 필요한 초를 올림으로 반환한다.
  public synchronized long retryAfterSeconds() {
    refill();
    if (availableTokens >= 1) {
      return 0;
    }

    return (long) Math.ceil((1 - availableTokens) / refillPerSec);
  }

  private void refill() {
    var now = nanoClock.getAsLong();
    var elapsedSec = (now - lastRefillNanos) / 1_000_000_000.0;

    // 보충은 "마지막 보충 이후 경과 시간 * refillPerSec"로 계산하되 capacity를 넘지 않는다.
    availableTokens = Math.min(capacity, availableTokens + elapsedSec * refillPerSec);
    lastRefillNanos = now;
  }
}
