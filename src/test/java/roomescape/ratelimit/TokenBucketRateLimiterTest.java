package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    @Test
    void capacity만큼_통과한_뒤_거부된다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void retryAfterSeconds는_토큰_하나가_찰_때까지_필요한_초를_올림으로_반환한다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 2.0, clock::get);

        assertThat(limiter.tryConsume()).isTrue();

        assertThat(limiter.retryAfterSeconds()).isEqualTo(1L);
    }

    @Test
    void 시간이_지나면_refillPerSec만큼_토큰이_보충된다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);
        limiter.tryConsume();
        limiter.tryConsume();
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(1_000_000_000L);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 보충된_토큰은_capacity를_넘지_않는다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        clock.addAndGet(100_000_000_000L);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 동시_요청에서도_capacity개만_통과한다() throws InterruptedException {
        int capacity = 3;
        int threadCount = 20;
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger passed = new AtomicInteger();
        var pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    if (limiter.tryConsume()) {
                        passed.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        ready.await();
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(passed.get()).isEqualTo(capacity);
    }

    @Test
    void capacity와_refillPerSec는_0보다_커야_한다() {
        AtomicLong clock = new AtomicLong(0);

        assertThatThrownBy(() -> new TokenBucketRateLimiter(0, 1.0, clock::get))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TokenBucketRateLimiter(1, 0.0, clock::get))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
