package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    @Test
    void capacity만큼_요청을_허용한다() {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1, () -> 0L);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void 경과_시간에_따라_토큰을_보충한다() {
        AtomicLong clock = new AtomicLong();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 2, clock::get);
        rateLimiter.tryConsume();
        rateLimiter.tryConsume();

        clock.addAndGet(Duration.ofMillis(500).toNanos());

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void 토큰_하나가_생길_때까지_남은_시간을_올림한다() {
        AtomicLong clock = new AtomicLong();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 0.5, clock::get);
        rateLimiter.tryConsume();

        clock.addAndGet(Duration.ofMillis(100).toNanos());

        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(2);
    }

    @Test
    void 동시_요청에서도_capacity만큼만_통과한다() throws Exception {
        int capacity = 10;
        int requestCount = 100;
        TokenBucketRateLimiter rateLimiter =
            new TokenBucketRateLimiter(capacity, 1, () -> 0L);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int index = 0; index < requestCount; index++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return rateLimiter.tryConsume();
                }));
            }
            ready.await();
            start.countDown();

            long successCount = 0;
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successCount++;
                }
            }
            assertThat(successCount).isEqualTo(capacity);
        }
    }
}
