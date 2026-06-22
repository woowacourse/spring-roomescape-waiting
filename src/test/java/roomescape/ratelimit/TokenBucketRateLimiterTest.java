package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    private final AtomicLong now = new AtomicLong();

    @Test
    void capacity만큼_요청을_통과시키고_이후_요청은_거부한다() {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1.0, now::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void 경과_시간과_초당_보충량에_비례해_토큰을_보충한다() {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 2.0, now::get);
        rateLimiter.tryConsume();
        rateLimiter.tryConsume();
        rateLimiter.tryConsume();

        now.addAndGet(500_000_000L);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void retryAfterSeconds는_토큰_하나가_찰_때까지의_시간을_올림한다() {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2.0, now::get);
        rateLimiter.tryConsume();

        now.addAndGet(100_000_000L);

        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    void 동시_요청에서도_capacity개만_통과한다() throws InterruptedException {
        int capacity = 5;
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(capacity, 1.0, now::get);
        CountDownLatch ready = new CountDownLatch(20);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(20)) {
            for (int i = 0; i < 20; i++) {
                executor.submit(() -> {
                    ready.countDown();
                    await(start);
                    if (rateLimiter.tryConsume()) {
                        successCount.incrementAndGet();
                    }
                });
            }
            ready.await(1, TimeUnit.SECONDS);
            start.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(successCount.get()).isEqualTo(capacity);
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
