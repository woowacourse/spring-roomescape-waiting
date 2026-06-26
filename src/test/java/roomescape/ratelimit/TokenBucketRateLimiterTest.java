package roomescape.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketRateLimiterTest {

    private final AtomicLong nanos = new AtomicLong(0);

    private TokenBucketRateLimiter limiter(long capacity, double refillPerSec) {
        return new TokenBucketRateLimiter(capacity, refillPerSec, nanos::get);
    }

    private void advanceSeconds(double seconds) {
        nanos.addAndGet((long) (seconds * 1_000_000_000L));
    }

    @Test
    @DisplayName("capacity만큼만 통과하고 그 이상은 거부한다.")
    void consumeUpToCapacity() {
        TokenBucketRateLimiter limiter = limiter(3, 1);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("토큰이 비면 retryAfterSeconds를 올림으로 반환한다.")
    void retryAfterSeconds() {
        TokenBucketRateLimiter limiter = limiter(1, 2);
        limiter.tryConsume();

        assertThat(limiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    @DisplayName("경과 시간 × refillPerSec만큼 보충하되 capacity를 넘지 않는다.")
    void refillUpToCapacity() {
        TokenBucketRateLimiter limiter = limiter(3, 2);
        limiter.tryConsume();
        limiter.tryConsume();
        limiter.tryConsume();

        advanceSeconds(1);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();

        advanceSeconds(100);
        for (int i = 0; i < 3; i++) {
            assertThat(limiter.tryConsume()).isTrue();
        }
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("동시 요청에서도 정확히 capacity개만 통과한다.")
    void concurrentConsume() throws InterruptedException {
        int capacity = 100;
        int threads = 500;
        TokenBucketRateLimiter limiter = limiter(capacity, 1);
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger passed = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    if (limiter.tryConsume()) {
                        passed.incrementAndGet();
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        executor.shutdown();

        assertThat(passed.get()).isEqualTo(capacity);
    }
}
