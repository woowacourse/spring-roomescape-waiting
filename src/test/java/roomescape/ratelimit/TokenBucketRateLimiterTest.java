package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    @Test
    @DisplayName("초기 capacity만큼만 통과시키고 이후 요청은 거부한다.")
    void consumes_initial_capacity_only() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1, clock::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    @DisplayName("경과 시간과 refillPerSecond만큼 토큰을 보충하되 capacity를 넘지 않는다.")
    void refills_by_elapsed_time_without_exceeding_capacity() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 2, clock::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();

        clock.addAndGet(NANOS_PER_SECOND / 2);
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();

        clock.addAndGet(NANOS_PER_SECOND);
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();

        clock.addAndGet(10 * NANOS_PER_SECOND);
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("토큰이 있으면 Retry-After는 0이고, 토큰이 없으면 1개가 찰 때까지 필요한 초를 올림한다.")
    void calculates_retry_after_seconds() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 4, clock::get);

        assertThat(rateLimiter.retryAfterSeconds()).isZero();

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);

        clock.addAndGet(NANOS_PER_SECOND / 5);
        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);

        clock.addAndGet(NANOS_PER_SECOND / 20);
        assertThat(rateLimiter.retryAfterSeconds()).isZero();
    }

    @Test
    @DisplayName("동시 요청에서도 정확히 capacity개만 통과한다.")
    void consumes_safely_under_concurrency() throws InterruptedException {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(5, 1, () -> 0L);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch ready = new CountDownLatch(20);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            tasks.add(() -> {
                ready.countDown();
                try {
                    start.await();
                    if (rateLimiter.tryConsume()) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        tasks.forEach(executorService::submit);
        ready.await(1, TimeUnit.SECONDS);
        start.countDown();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(successCount).hasValue(5);
    }
}
