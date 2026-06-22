package roomescape.infrastructure.ratelimit;

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
    @DisplayName("capacity만큼 통과한 뒤 거부되고 retryAfter는 보충 시간이다")
    void consumesUpToCapacityThenRejects() {
        var clock = new AtomicLong(0);
        var limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
        assertThat(limiter.retryAfterSeconds()).isEqualTo(1L);
    }

    @Test
    @DisplayName("N초 경과하면 토큰이 보충되어 다시 통과한다")
    void refillsAfterElapsedTime() {
        var clock = new AtomicLong(0);
        var limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);
        limiter.tryConsume();
        limiter.tryConsume();
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(1_000_000_000L);

        assertThat(limiter.tryConsume()).isTrue();
    }

    @Test
    @DisplayName("refillPerSec가 평균 TPS 상한이 된다")
    void refillPerSecIsAverageTpsLimit() {
        var clock = new AtomicLong(0);
        var limiter = new TokenBucketRateLimiter(1, 2.0, clock::get);
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(500_000_000L);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("오래 기다려도 토큰은 capacity를 넘게 쌓이지 않는다")
    void tokensNeverExceedCapacity() {
        var clock = new AtomicLong(0);
        var limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        clock.addAndGet(100_000_000_000L);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @ParameterizedTest(name = "capacity={0} -> {1}번째 요청에서 거부")
    @CsvSource({"5, 6", "1, 2", "3, 4"})
    @DisplayName("한도 파라미터에 따라 거부 시점이 달라진다")
    void rejectionPointDependsOnCapacity(long capacity, int rejectAt) {
        var clock = new AtomicLong(0);
        var limiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);

        for (var i = 1; i < rejectAt; i++) {
            assertThat(limiter.tryConsume()).as("%d번째 요청", i).isTrue();
        }
        assertThat(limiter.tryConsume()).as("%d번째 요청은 거부", rejectAt).isFalse();
    }

    @Test
    @DisplayName("동시 요청 여러 개 중 정확히 capacity개만 통과한다")
    void onlyCapacityPassesUnderConcurrency() throws InterruptedException {
        var capacity = 3;
        var threadCount = 20;
        var clock = new AtomicLong(0);
        var limiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);

        var ready = new CountDownLatch(threadCount);
        var start = new CountDownLatch(1);
        var passed = new AtomicInteger();
        var pool = Executors.newFixedThreadPool(threadCount);

        for (var i = 0; i < threadCount; i++) {
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