package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    @Test
    void capacity만큼만_즉시_소비할_수_있다() {
        AtomicLong clock = new AtomicLong();
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1, clock::get);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 경과_시간과_refillPerSec에_따라_토큰이_보충된다() {
        AtomicLong clock = new AtomicLong();
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 2, clock::get);
        limiter.tryConsume();
        limiter.tryConsume();

        clock.addAndGet(Duration.ofMillis(499).toNanos());
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(Duration.ofMillis(1).toNanos());
        assertThat(limiter.tryConsume()).isTrue();
    }

    @Test
    void 한_토큰이_찰_때까지_남은_시간을_초_단위로_올림한다() {
        AtomicLong clock = new AtomicLong();
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 0.4, clock::get);
        limiter.tryConsume();

        assertThat(limiter.retryAfterSeconds()).isEqualTo(3);

        clock.addAndGet(Duration.ofMillis(600).toNanos());

        assertThat(limiter.retryAfterSeconds()).isEqualTo(2);
    }

    @Test
    void 동시_요청에서도_capacity개만_통과한다() throws InterruptedException {
        int capacity = 20;
        int requestCount = 100;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1, () -> 0L);
        AtomicInteger passed = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < requestCount; i++) {
                executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    if (limiter.tryConsume()) {
                        passed.incrementAndGet();
                    }
                    return null;
                });
            }
            assertThat(ready.await(1, TimeUnit.SECONDS)).isTrue();
            start.countDown();
        }

        assertThat(passed).hasValue(capacity);
    }
}
