package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    @Test
    void 토큰이_있으면_소비하고_통과한다() {
        FakeNanoTime nanoTime = new FakeNanoTime();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1, nanoTime);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void 경과_시간에_따라_토큰을_보충한다() {
        FakeNanoTime nanoTime = new FakeNanoTime();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 2, nanoTime);

        rateLimiter.tryConsume();
        rateLimiter.tryConsume();
        nanoTime.advanceSeconds(1);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void 토큰이_없으면_다음_토큰까지_필요한_초를_올림으로_계산한다() {
        FakeNanoTime nanoTime = new FakeNanoTime();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2, nanoTime);

        rateLimiter.tryConsume();

        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    void 동시에_요청해도_capacity_개수만_통과한다() throws InterruptedException {
        FakeNanoTime nanoTime = new FakeNanoTime();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(10, 0, nanoTime);
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        CountDownLatch ready = new CountDownLatch(30);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger consumed = new AtomicInteger();

        for (int i = 0; i < 30; i++) {
            executorService.submit(() -> {
                ready.countDown();
                start.await();
                if (rateLimiter.tryConsume()) {
                    consumed.incrementAndGet();
                }
                return null;
            });
        }

        ready.await(1, TimeUnit.SECONDS);
        start.countDown();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(consumed).hasValue(10);
    }

    private static class FakeNanoTime implements java.util.function.LongSupplier {

        private long current;

        @Override
        public long getAsLong() {
            return current;
        }

        private void advanceSeconds(long seconds) {
            current += TimeUnit.SECONDS.toNanos(seconds);
        }
    }
}
