package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    @Test
    @DisplayName("capacity만큼 즉시 통과하고 이후 요청은 거부한다")
    void tryConsume_capacity() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1, now::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    @DisplayName("경과 시간에 refillPerSec를 곱한 만큼 보충하고 capacity를 넘지 않는다")
    void tryConsume_refill() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 2, now::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();

        now.addAndGet(500_000_000L);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();

        now.addAndGet(10_000_000_000L);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("1개 토큰이 찰 때까지 필요한 초를 올림으로 반환한다")
    void retryAfterSeconds_roundsUp() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2, now::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        now.addAndGet(100_000_000L);

        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    @DisplayName("동시 요청에서도 capacity개만 통과한다")
    void tryConsume_concurrent() throws InterruptedException {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(10, 1, () -> 0);
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(20);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < 20; i++) {
            executorService.submit(() -> {
                try {
                    start.await();
                    if (rateLimiter.tryConsume()) {
                        success.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertThat(done.await(1, TimeUnit.SECONDS)).isTrue();
        executorService.shutdown();

        assertThat(success.get()).isEqualTo(10);
    }
}
