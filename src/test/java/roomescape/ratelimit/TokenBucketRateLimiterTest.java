package roomescape.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenBucketRateLimiterTest {

    @Test
    void capacity만큼_요청을_통과시키고_이후_요청은_거부한다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1L);
    }

    @Test
    void 시간이_지나면_경과_시간과_refillPerSecond에_비례해_토큰이_보충된다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1.0, clock::get);
        rateLimiter.tryConsume();
        rateLimiter.tryConsume();

        clock.addAndGet(1_000_000_000L);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void 토큰은_capacity를_넘어_보충되지_않는다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        clock.addAndGet(100_000_000_000L);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void retryAfterSeconds는_토큰_하나가_찰_때까지_필요한_초를_올림해서_반환한다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2.0, clock::get);
        rateLimiter.tryConsume();

        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1L);

        clock.addAndGet(500_000_000L);

        assertThat(rateLimiter.retryAfterSeconds()).isZero();
    }

    @Test
    void 동시_요청에서도_capacity개만_통과한다() throws InterruptedException {
        int capacity = 3;
        int threadCount = 20;
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger passed = new AtomicInteger();
        var executorService = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    if (rateLimiter.tryConsume()) {
                        passed.incrementAndGet();
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        ready.await();
        start.countDown();
        executorService.shutdown();

        assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(passed.get()).isEqualTo(capacity);
    }

    @Test
    void capacity와_refillPerSecond는_0보다_커야한다() {
        assertThatThrownBy(() -> new TokenBucketRateLimiter(0, 1.0, System::nanoTime))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TokenBucketRateLimiter(1, 0, System::nanoTime))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
