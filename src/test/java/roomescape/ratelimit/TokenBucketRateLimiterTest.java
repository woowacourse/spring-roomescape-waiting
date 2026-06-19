package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    @DisplayName("capacity만큼 즉시 통과하고 이후 요청은 거부한다.")
    @Test
    void consumeCapacity() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1, now::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @DisplayName("경과 시간과 refillPerSecond만큼 토큰을 보충하되 capacity를 넘지 않는다.")
    @Test
    void refill() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 2, now::get);
        rateLimiter.tryConsume();
        rateLimiter.tryConsume();
        rateLimiter.tryConsume();

        now.addAndGet(500_000_000L);
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();

        now.addAndGet(10_000_000_000L);
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @DisplayName("토큰이 없으면 1개가 찰 때까지 필요한 초를 올림으로 반환한다.")
    @Test
    void retryAfterSeconds() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2, now::get);
        rateLimiter.tryConsume();

        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
        now.addAndGet(500_000_000L);

        assertThat(rateLimiter.retryAfterSeconds()).isZero();
    }

    @DisplayName("동시에 요청해도 capacity개만 통과한다.")
    @Test
    void concurrentConsume() throws Exception {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(5, 1, () -> 0L);
        int threads = 20;
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                ready.countDown();
                start.await();
                if (rateLimiter.tryConsume()) {
                    successCount.incrementAndGet();
                }
                return null;
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
        assertThat(successCount).hasValue(5);
    }
}
