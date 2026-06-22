package roomescape.common.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenBucketRateLimiterTest {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final AtomicLong currentNanos = new AtomicLong();

    @Test
    @DisplayName("capacity만큼 요청을 통과시키고 이후 요청은 거부한다")
    void consumeUntilCapacity() {
        final TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 1, currentNanos::get);

        assertThat(List.of(
                rateLimiter.tryConsume(),
                rateLimiter.tryConsume(),
                rateLimiter.tryConsume(),
                rateLimiter.tryConsume()
        )).containsExactly(true, true, true, false);
    }

    @Test
    @DisplayName("경과 시간과 refillPerSec만큼 토큰을 보충하되 capacity를 넘지 않는다")
    void refillTokensByElapsedTimeWithoutExceedingCapacity() {
        final TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 2, currentNanos::get);
        consumeAll(rateLimiter, 3);

        currentNanos.addAndGet(NANOS_PER_SECOND);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();

        currentNanos.addAndGet(10 * NANOS_PER_SECOND);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("토큰 1개가 보충되기까지 필요한 초를 올림으로 계산한다")
    void calculateRetryAfterSecondsByCeiling() {
        final TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2, currentNanos::get);
        rateLimiter.tryConsume();

        currentNanos.addAndGet(499_000_000L);

        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);

        currentNanos.addAndGet(1_000_000L);

        assertThat(rateLimiter.retryAfterSeconds()).isZero();
        assertThat(rateLimiter.tryConsume()).isTrue();
    }

    @Test
    @DisplayName("동시 요청에서도 정확히 capacity개만 통과한다")
    void consumeSafelyInConcurrentRequests() throws InterruptedException {
        final int capacity = 20;
        final TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(capacity, 1, currentNanos::get);
        final int threadCount = 100;
        final CountDownLatch ready = new CountDownLatch(threadCount);
        final CountDownLatch start = new CountDownLatch(1);
        final AtomicInteger successCount = new AtomicInteger();

        try (var executor = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    ready.countDown();
                    await(start);
                    if (rateLimiter.tryConsume()) {
                        successCount.incrementAndGet();
                    }
                });
            }

            ready.await();
            start.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(successCount).hasValue(capacity);
    }

    @Test
    @DisplayName("capacity와 refillPerSec은 1 이상이어야 한다")
    void validateProperties() {
        assertThatThrownBy(() -> new TokenBucketRateLimiter(0, 1, currentNanos::get))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacity");
        assertThatThrownBy(() -> new TokenBucketRateLimiter(1, 0, currentNanos::get))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("refillPerSec");
    }

    private void consumeAll(final TokenBucketRateLimiter rateLimiter, final int count) {
        for (int i = 0; i < count; i++) {
            rateLimiter.tryConsume();
        }
    }

    private void await(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
