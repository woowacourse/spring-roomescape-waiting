package roomescape.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketRateLimiterTest {

    @Test
    void capacity만큼만_즉시_소비할_수_있다() {
        FakeNanoClock clock = new FakeNanoClock();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1, clock::now);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void 경과_시간과_refillPerSec에_비례해_토큰을_보충한다() {
        FakeNanoClock clock = new FakeNanoClock();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 2, clock::now);

        rateLimiter.tryConsume();
        rateLimiter.tryConsume();
        rateLimiter.tryConsume();

        clock.advanceSeconds(0.5);
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();

        clock.advanceSeconds(1);
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void retryAfterSeconds는_토큰_1개가_찰_때까지_필요한_시간을_올림한다() {
        FakeNanoClock clock = new FakeNanoClock();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2, clock::now);

        rateLimiter.tryConsume();

        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    void 동시_요청에서도_capacity개만_통과한다() throws InterruptedException {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(5, 1, System::nanoTime);
        AtomicInteger successCount = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(20);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(20);

        try (var executor = Executors.newFixedThreadPool(20)) {
            for (int i = 0; i < 20; i++) {
                executor.submit(() -> {
                    ready.countDown();
                    await(start);
                    if (rateLimiter.tryConsume()) {
                        successCount.incrementAndGet();
                    }
                    done.countDown();
                });
            }
            ready.await(1, TimeUnit.SECONDS);
            start.countDown();
            done.await(1, TimeUnit.SECONDS);
        }

        assertThat(successCount).hasValue(5);
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private static class FakeNanoClock {

        private long now;

        long now() {
            return now;
        }

        void advanceSeconds(double seconds) {
            now += (long) (seconds * 1_000_000_000L);
        }
    }
}
