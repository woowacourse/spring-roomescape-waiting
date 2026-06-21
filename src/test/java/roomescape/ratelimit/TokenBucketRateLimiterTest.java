package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TokenBucketRateLimiterTest {

    @Test
    void capacity만큼_통과한뒤_거부되고_retryAfter는_보충시간이다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
        assertThat(limiter.retryAfterSeconds()).isEqualTo(1L);
    }

    @Test
    void N초_경과하면_토큰이_보충되어_다시_통과한다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);
        limiter.tryConsume();
        limiter.tryConsume();
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(1_000_000_000L); // +1초 → +1 토큰

        assertThat(limiter.tryConsume()).isTrue();
    }

    @Test
    void refillPerSec가_평균_TPS상한이_된다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 2.0, clock::get);
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(500_000_000L); // +0.5초 → 2.0 × 0.5 = 1개 보충

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 오래_기다려도_토큰은_capacity를_넘게_쌓이지_않는다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        clock.addAndGet(100_000_000_000L); // +100초

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @ParameterizedTest(name = "capacity={0} → {1}번째 요청에서 거부")
    @CsvSource({"5, 6", "1, 2", "3, 4"})
    void 한도_파라미터에_따라_거부시점이_달라진다(long capacity, int rejectAt) {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);

        for (int i = 1; i < rejectAt; i++) {
            assertThat(limiter.tryConsume()).as("%d번째 요청", i).isTrue();
        }
        assertThat(limiter.tryConsume()).as("%d번째 요청은 거부", rejectAt).isFalse();
    }

    @Test
    void 동시요청_여러개중_capacity개만_통과한다() throws InterruptedException {
        int capacity = 3;
        int threadCount = 20;
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger passed = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

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
}
