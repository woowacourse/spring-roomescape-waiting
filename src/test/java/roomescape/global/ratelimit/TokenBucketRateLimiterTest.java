package roomescape.global.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketRateLimiterTest {

    private static final long NANOS_PER_SEC = 1_000_000_000L;

    @Test
    @DisplayName("초기 토큰은 capacity만큼 채워져 있다.")
    void initialTokens_equalsCapacity() {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1.0, () -> now[0]);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("토큰이 소진되면 tryConsume()은 false를 반환한다.")
    void tryConsume_whenExhausted_returnsFalse() {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, () -> now[0]);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("시간이 경과하면 refillPerSec에 비례해 토큰이 보충된다.")
    void refill_afterElapsed_addsTokens() {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 2.0, () -> now[0]);

        // capacity 모두 소비
        for (int i = 0; i < 5; i++) {
            limiter.tryConsume();
        }
        assertThat(limiter.tryConsume()).isFalse();

        // 1초 경과 → 2개 보충
        now[0] += NANOS_PER_SEC;
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("보충된 토큰은 capacity를 초과하지 않는다.")
    void refill_doesNotExceedCapacity() {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 10.0, () -> now[0]);

        // capacity 모두 소비
        for (int i = 0; i < 3; i++) {
            limiter.tryConsume();
        }

        // 10초 경과 → 100개가 보충되려 하지만 capacity(3)에서 멈춤
        now[0] += 10 * NANOS_PER_SEC;
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("retryAfterSeconds()는 1개가 찰 때까지 걸리는 시간을 올림으로 반환한다.")
    void retryAfterSeconds_returnsRoundedUpWaitTime() {
        long[] now = {0L};
        // refillPerSec = 2.0 → 1개 채우는 데 0.5초 → ceil(0.5) = 1
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 2.0, () -> now[0]);

        limiter.tryConsume(); // 소진
        assertThat(limiter.retryAfterSeconds()).isEqualTo(1L);
    }

    @Test
    @DisplayName("retryAfterSeconds()는 refillPerSec가 낮을수록 대기 시간이 길다.")
    void retryAfterSeconds_slowRefill_returnsLongerWait() {
        long[] now = {0L};
        // refillPerSec = 0.5 → 1개 채우는 데 2초 → ceil(2.0) = 2
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 0.5, () -> now[0]);

        limiter.tryConsume(); // 소진
        assertThat(limiter.retryAfterSeconds()).isEqualTo(2L);
    }

    @Test
    @DisplayName("동시 요청에서 정확히 capacity개만 통과한다.")
    void tryConsume_concurrentRequests_onlyCapacityPass() throws InterruptedException {
        int capacity = 5;
        int threadCount = 20;
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 0.0, () -> now[0]);

        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    if (limiter.tryConsume()) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(capacity);
    }

    @Test
    @DisplayName("capacity 0으로 생성하면 모든 요청이 거부된다.")
    void tryConsume_withZeroCapacity_alwaysReturnsFalse() {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(0, 0.0, () -> now[0]);

        assertThat(limiter.tryConsume()).isFalse();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("소수점 보충이 누적되어 1 이상이 되면 통과한다.")
    void refill_fractionalAccumulation_eventuallyAllows() {
        long[] now = {0L};
        // refillPerSec = 2.0 → 0.5초마다 1개 보충
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 2.0, () -> now[0]);

        limiter.tryConsume(); // 소진
        assertThat(limiter.tryConsume()).isFalse();

        // 0.4초 경과 → 0.8개 보충 (아직 1개 미만)
        now[0] += (long) (0.4 * NANOS_PER_SEC);
        assertThat(limiter.tryConsume()).isFalse();

        // 추가 0.2초 경과 → 누적 0.4 + 0.4 = 0.8 + 0.4 = 1.2개 → 통과
        now[0] += (long) (0.2 * NANOS_PER_SEC);
        assertThat(limiter.tryConsume()).isTrue();
    }
}
