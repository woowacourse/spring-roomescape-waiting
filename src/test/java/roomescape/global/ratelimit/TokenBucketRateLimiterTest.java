package roomescape.global.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketRateLimiterTest {

    @Test
    @DisplayName("capacity만큼 통과한 뒤 거부되고 Retry-After는 다음 토큰 보충 시간이다.")
    void tryConsume_rejectsAfterCapacity() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    @DisplayName("마지막 보충 이후 경과 시간만큼 토큰을 보충한다.")
    void tryConsume_refillsByElapsedTime() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2.0, clock::get);
        rateLimiter.tryConsume();

        assertThat(rateLimiter.tryConsume()).isFalse();

        clock.addAndGet(500_000_000L);

        assertThat(rateLimiter.tryConsume()).isTrue();
    }

    @Test
    @DisplayName("오래 기다려도 토큰은 capacity를 넘지 않는다.")
    void tryConsume_doesNotRefillBeyondCapacity() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        clock.addAndGet(100_000_000_000L);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @ParameterizedTest(name = "capacity={0}이면 {1}번째 요청에서 거부된다.")
    @CsvSource({"1, 2", "3, 4", "5, 6"})
    void tryConsume_rejectPointFollowsCapacity(long capacity, int rejectAt) {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);

        for (int i = 1; i < rejectAt; i++) {
            assertThat(rateLimiter.tryConsume()).as("%d번째 요청", i).isTrue();
        }
        assertThat(rateLimiter.tryConsume()).as("%d번째 요청", rejectAt).isFalse();
    }

    @Test
    @DisplayName("동시 요청에서도 정확히 capacity개만 통과한다.")
    void tryConsume_allowsOnlyCapacityUnderConcurrency() throws InterruptedException {
        int capacity = 3;
        int threadCount = 20;
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger passed = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
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
        executor.shutdown();

        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(passed.get()).isEqualTo(capacity);
    }
}
