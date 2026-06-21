package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    @Test
    @DisplayName("capacity 만큼만 즉시 통과하고 이후 요청은 거부한다.")
    void consumesOnlyCapacity() {
        FakeNanoClock clock = new FakeNanoClock();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1.0, clock);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("경과 시간과 refillPerSec에 비례해 토큰을 보충하되 capacity를 넘지 않는다.")
    void refillsByElapsedTimeWithoutExceedingCapacity() {
        FakeNanoClock clock = new FakeNanoClock();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 2.0, clock);

        rateLimiter.tryConsume();
        rateLimiter.tryConsume();

        clock.advanceMillis(500);
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();

        clock.advanceMillis(2_000);
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("retryAfterSeconds는 토큰 1개가 찰 때까지 필요한 초를 올림해 반환한다.")
    void retryAfterSecondsReturnsCeilingSecondsUntilNextToken() {
        FakeNanoClock clock = new FakeNanoClock();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2.0, clock);

        rateLimiter.tryConsume();

        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    @DisplayName("동시에 몰린 요청도 capacity 개수만 통과한다.")
    void allowsOnlyCapacityUnderConcurrency() throws InterruptedException {
        FakeNanoClock clock = new FakeNanoClock();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 1.0, clock);
        AtomicInteger passed = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(20);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(20);
        var executor = Executors.newFixedThreadPool(20);

        for (int i = 0; i < 20; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    if (rateLimiter.tryConsume()) {
                        passed.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        executor.shutdown();

        assertThat(passed).hasValue(3);
    }

    private static class FakeNanoClock implements LongSupplier {

        private long now;

        @Override
        public long getAsLong() {
            return now;
        }

        void advanceMillis(long millis) {
            now += millis * 1_000_000L;
        }
    }
}
