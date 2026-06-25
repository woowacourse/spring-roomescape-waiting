package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    @Test
    @DisplayName("최초에는 capacity 개수만큼만 요청을 통과시킨다.")
    void consumeUpToCapacity() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 1, now::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("마지막 보충 이후 경과 시간과 초당 보충량만큼 토큰을 보충한다.")
    void refillByElapsedTime() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 2, now::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();

        now.addAndGet(500_000_000L);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("토큰은 capacity 를 초과해서 보충되지 않는다.")
    void refillDoesNotExceedCapacity() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 10, now::get);

        assertThat(rateLimiter.tryConsume()).isTrue();

        now.addAndGet(10 * NANOS_PER_SECOND);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("토큰이 남아있으면 재시도 대기 시간은 0초다.")
    void retryAfterSecondsReturnsZeroWhenTokenIsAvailable() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1, now::get);

        assertThat(rateLimiter.retryAfterSeconds()).isZero();
    }

    @Test
    @DisplayName("토큰이 없으면 1개가 찰 때까지 필요한 시간을 올림해서 반환한다.")
    void retryAfterSecondsReturnsCeilingSecondsUntilNextToken() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2, now::get);

        assertThat(rateLimiter.tryConsume()).isTrue();

        now.addAndGet(100_000_000L);

        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    @DisplayName("동시 요청에서도 capacity 개수만 정확히 통과시킨다.")
    void consumeConcurrentlyUpToCapacity() throws Exception {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(5, 1, now::get);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch ready = new CountDownLatch(10);
        CountDownLatch start = new CountDownLatch(1);

        List<Callable<Boolean>> tasks = java.util.stream.IntStream.range(0, 10)
                .mapToObj(ignored -> (Callable<Boolean>) () -> {
                    ready.countDown();
                    start.await();
                    return rateLimiter.tryConsume();
                })
                .toList();

        List<Future<Boolean>> futures = tasks.stream()
                .map(executorService::submit)
                .toList();

        ready.await();
        start.countDown();

        long successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(successCount).isEqualTo(5);
    }
}
