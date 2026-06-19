package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    private static final class FakeClock implements LongSupplier {
        private long nanos;

        FakeClock(long startNanos) {
            this.nanos = startNanos;
        }

        void advanceSeconds(double seconds) {
            nanos += (long) (seconds * 1_000_000_000L);
        }

        @Override
        public long getAsLong() {
            return nanos;
        }
    }

    @Test
    void capacity만큼_연속_통과_후_거부된다() {
        FakeClock clock = new FakeClock(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1, clock);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 경과시간만큼_보충되되_capacity를_넘지_않는다() {
        FakeClock clock = new FakeClock(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 10, clock);

        limiter.tryConsume();
        limiter.tryConsume();
        assertThat(limiter.tryConsume()).isFalse();

        clock.advanceSeconds(0.1);
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();

        clock.advanceSeconds(100);
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void retryAfterSeconds는_올림으로_계산된다() {
        FakeClock clock = new FakeClock(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 10, clock);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.retryAfterSeconds()).isEqualTo(1);

        clock.advanceSeconds(0.1);
        assertThat(limiter.retryAfterSeconds()).isEqualTo(0);
    }

    @Test
    void 동시_요청에서도_정확히_capacity개만_통과한다() throws InterruptedException {
        FakeClock clock = new FakeClock(0);
        int capacity = 50;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1, clock);

        int threadCount = 200;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger passed = new AtomicInteger();

        try {
            for (int i = 0; i < threadCount; i++) {
                pool.submit(() -> {
                    ready.countDown();
                    await(start);
                    if (limiter.tryConsume()) {
                        passed.incrementAndGet();
                    }
                });
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            pool.shutdown();
            assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        } finally {
            pool.shutdownNow();
        }

        assertThat(passed.get()).isEqualTo(capacity);
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
