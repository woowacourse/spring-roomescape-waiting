package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TokenBucketRateLimiterTest {

    @Test
    @DisplayName("capacity만큼 통과한 뒤 거부되고 retryAfter는 보충에 필요한 초다")
    void capacity만큼_통과한뒤_거부된다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
        assertThat(limiter.retryAfterSeconds()).isEqualTo(1L);
    }

    @Test
    @DisplayName("시간이 경과하면 토큰이 보충되어 다시 통과한다")
    void 시간이_경과하면_보충되어_통과한다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);
        limiter.tryConsume();
        limiter.tryConsume();
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(1_000_000_000L);

        assertThat(limiter.tryConsume()).isTrue();
    }

    @Test
    @DisplayName("refillPerSec가 평균 TPS 상한이 된다")
    void refillPerSec가_평균_TPS상한이_된다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 2.0, clock::get);
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(500_000_000L);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("오래 기다려도 토큰은 capacity를 넘게 쌓이지 않는다")
    void 토큰은_capacity를_넘지_않는다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        clock.addAndGet(100_000_000_000L);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @ParameterizedTest(name = "capacity={0} -> {1}번째 요청에서 거부")
    @CsvSource({"5, 6", "1, 2", "3, 4"})
    @DisplayName("한도 파라미터에 따라 거부 시점이 달라진다")
    void 한도에_따라_거부시점이_달라진다(long capacity, int rejectAt) {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);

        for (int i = 1; i < rejectAt; i++) {
            assertThat(limiter.tryConsume()).as("%d번째 요청", i).isTrue();
        }
        assertThat(limiter.tryConsume()).as("%d번째 요청은 거부", rejectAt).isFalse();
    }

    @Test
    @DisplayName("동시 요청 여러 개 중 정확히 capacity개만 통과한다")
    void 동시요청중_capacity개만_통과한다() throws InterruptedException {
        int capacity = 3;
        int threadCount = 20;
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger passed = new AtomicInteger();
        var pool = Executors.newFixedThreadPool(threadCount);

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