package roomescape.payment.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    @Test
    @DisplayName("capacity만큼은 즉시 통과하고 그 다음 요청은 거부된다")
    void burstUpToCapacity() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1, now::get);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("시간이 지나면 refillPerSec에 비례해 토큰이 보충된다")
    void refillsOverTime() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 2, now::get);
        limiter.tryConsume();
        limiter.tryConsume();
        assertThat(limiter.tryConsume()).isFalse();

        now.set(500_000_000L);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("보충은 capacity를 넘지 않는다")
    void refillCappedAtCapacity() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 10, now::get);
        limiter.tryConsume();
        limiter.tryConsume();

        now.set(10_000_000_000L);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("토큰이 있으면 retryAfterSeconds는 0이다")
    void retryAfterZeroWhenAvailable() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1, now::get);

        assertThat(limiter.retryAfterSeconds()).isZero();
    }

    @Test
    @DisplayName("토큰이 없으면 1개가 찰 때까지 필요한 초를 올림으로 반환한다")
    void retryAfterCeilsToSeconds() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 2, now::get);
        limiter.tryConsume();

        assertThat(limiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    @DisplayName("동시에 요청해도 정확히 capacity개만 통과한다")
    void concurrentlyAllowsExactlyCapacity() throws InterruptedException {
        AtomicLong now = new AtomicLong(0);
        int capacity = 50;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 0, now::get);

        int threads = 200;
        ExecutorService executor = Executors.newFixedThreadPool(32);
        AtomicInteger passed = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                if (limiter.tryConsume()) {
                    passed.incrementAndGet();
                }
            });
        }
        executor.shutdown();
        assertThat(executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();

        assertThat(passed.get()).isEqualTo(capacity);
    }
}
