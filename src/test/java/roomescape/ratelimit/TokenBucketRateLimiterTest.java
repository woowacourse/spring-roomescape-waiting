package roomescape.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenBucketRateLimiterTest {

    private static final long ONE_SECOND_NANOS = 1_000_000_000L;

    private final AtomicLong fakeNanos = new AtomicLong(0L);
    private final LongSupplier fakeClock = fakeNanos::get;

    private void advanceSeconds(double seconds) {
        fakeNanos.addAndGet((long) (seconds * ONE_SECOND_NANOS));
    }

    @Test
    void 초기에는_capacity개만큼_통과시키고_이후_거부한다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1, fakeClock);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 시간이_지나_보충되면_다시_통과시킨다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 2, fakeClock);
        limiter.tryConsume();
        limiter.tryConsume();
        assertThat(limiter.tryConsume()).isFalse();

        advanceSeconds(0.5);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 보충량은_capacity를_넘지_않는다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 10, fakeClock);
        for (int i = 0; i < 5; i++) {
            limiter.tryConsume();
        }

        advanceSeconds(100);

        for (int i = 0; i < 5; i++) {
            assertThat(limiter.tryConsume()).isTrue();
        }
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 토큰이_없으면_retryAfterSeconds가_보충_대기_시간을_올림으로_반환한다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 2, fakeClock);
        limiter.tryConsume();

        assertThat(limiter.retryAfterSeconds()).isEqualTo(1L);
    }

    @Test
    void 토큰이_남아_있으면_retryAfterSeconds가_0을_반환한다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1, fakeClock);

        assertThat(limiter.retryAfterSeconds()).isEqualTo(0L);
    }

    @Test
    void 생성자는_capacity가_1보다_작으면_예외를_던진다() {
        assertThatThrownBy(() -> new TokenBucketRateLimiter(0, 1, fakeClock))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 생성자는_refillPerSec가_0이하면_예외를_던진다() {
        assertThatThrownBy(() -> new TokenBucketRateLimiter(1, 0, fakeClock))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 동시_요청에서도_정확히_capacity개만_통과시킨다() throws InterruptedException {
        int capacity = 100;
        int threadCount = 1_000;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1, fakeClock);

        AtomicInteger passed = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ready.countDown();
                await(start);
                if (limiter.tryConsume()) {
                    passed.incrementAndGet();
                }
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(passed.get()).isEqualTo(capacity);
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
