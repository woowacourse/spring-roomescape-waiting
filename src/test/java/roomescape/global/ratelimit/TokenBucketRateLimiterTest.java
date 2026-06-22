package roomescape.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TokenBucketRateLimiterTest {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private long[] clock = {0L};

    private LongSupplier fakeClock() {
        return () -> clock[0];
    }

    private void advanceSeconds(double seconds) {
        clock[0] += (long) (seconds * NANOS_PER_SECOND);
    }

    @Test
    void capacity만큼만_통과하고_그_이후는_거부되며_retryAfterSeconds를_올림으로_반환한다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1.0, fakeClock());

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();

        assertThat(limiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    void 경과시간만큼_refillPerSec로_토큰이_보충된다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 1.0, fakeClock());
        for (int i = 0; i < 5; i++) {
            limiter.tryConsume();
        }
        assertThat(limiter.tryConsume()).isFalse();

        advanceSeconds(3);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void refillPerSec가_2이면_0_5초마다_1개씩_보충된다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 2.0, fakeClock());
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();

        advanceSeconds(0.5);
        assertThat(limiter.tryConsume()).isTrue();

        advanceSeconds(0.4);
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 오래_기다려도_capacity를_넘는_토큰은_쌓이지_않는다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 1.0, fakeClock());

        advanceSeconds(1000);

        for (int i = 0; i < 5; i++) {
            assertThat(limiter.tryConsume()).isTrue();
        }
        assertThat(limiter.tryConsume()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 5, 10})
    void capacity가_다르면_거부되는_요청_수도_달라진다(long capacity) {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1.0, fakeClock());

        long passed = IntStream.range(0, (int) capacity)
                .filter(i -> limiter.tryConsume())
                .count();

        assertThat(passed).isEqualTo(capacity);
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 동시_요청에서도_정확히_capacity개만_통과한다() throws InterruptedException {
        int capacity = 20;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 0.0, fakeClock());
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger passedCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                if (limiter.tryConsume()) {
                    passedCount.incrementAndGet();
                }
                latch.countDown();
            });
        }
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(passedCount.get()).isEqualTo(capacity);
    }
}
