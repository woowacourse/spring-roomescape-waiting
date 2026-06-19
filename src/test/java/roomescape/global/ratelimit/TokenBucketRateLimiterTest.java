package roomescape.global.ratelimit;

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

    @DisplayName("capacity만큼 통과한 뒤 거부되고 Retry-After는 다음 토큰 보충 시간입니다.")
    @Test
    void consume_until_capacity_and_calculate_retry_after() {
        // Given: 시계를 고정해서 자동 보충이 일어나지 않는 토큰 버킷을 준비합니다.
        AtomicLong clock = new AtomicLong(0L);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        // When: 버킷 용량보다 하나 더 많은 요청을 소비합니다.
        boolean first = rateLimiter.tryConsume();
        boolean second = rateLimiter.tryConsume();
        boolean third = rateLimiter.tryConsume();

        // Then: capacity 개수만 통과하고, 0개에서 초당 1개 보충되므로 Retry-After는 1초입니다.
        assertThat(first).isTrue();
        assertThat(second).isTrue();
        assertThat(third).isFalse();
        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1L);
    }

    @DisplayName("경과 시간에 비례해 토큰이 보충되어 다시 통과합니다.")
    @Test
    void refill_tokens_by_elapsed_time() {
        // Given: 토큰 2개를 모두 소비한 버킷을 준비합니다.
        AtomicLong clock = new AtomicLong(0L);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1.0, clock::get);
        rateLimiter.tryConsume();
        rateLimiter.tryConsume();

        // When: 1초가 지나 초당 1개 보충 정책으로 토큰 1개가 다시 찹니다.
        clock.addAndGet(1_000_000_000L);

        // Then: 다음 요청은 통과합니다.
        assertThat(rateLimiter.tryConsume()).isTrue();
    }

    @DisplayName("refillPerSec는 평균 TPS 상한을 의미합니다.")
    @Test
    void refill_per_second_defines_average_tps() {
        // Given: 초당 2개, 즉 0.5초에 1개씩 보충되는 버킷을 준비합니다.
        AtomicLong clock = new AtomicLong(0L);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2.0, clock::get);
        rateLimiter.tryConsume();

        // When: 0.5초가 지납니다.
        clock.addAndGet(500_000_000L);

        // Then: 토큰 1개가 보충되어 한 번만 다시 통과합니다.
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @DisplayName("오래 기다려도 토큰은 capacity를 넘어서 쌓이지 않습니다.")
    @Test
    void refill_does_not_exceed_capacity() {
        // Given: capacity가 2인 버킷을 준비합니다.
        AtomicLong clock = new AtomicLong(0L);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        // When: 상한이 없다면 100개가 쌓일 만큼 시간이 지납니다.
        clock.addAndGet(100_000_000_000L);

        // Then: 그래도 버스트 허용량은 capacity인 2개로 제한됩니다.
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @DisplayName("설정한 capacity에 따라 거부 시점이 달라집니다.")
    @CsvSource({"5, 6", "1, 2", "3, 4"})
    @ParameterizedTest
    void reject_at_capacity_plus_one(long capacity, int rejectAt) {
        // Given: capacity만 다른 버킷을 준비합니다.
        AtomicLong clock = new AtomicLong(0L);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);

        // When & Then: capacity 개수까지 통과하고, 그 다음 요청은 거부됩니다.
        for (int index = 1; index < rejectAt; index++) {
            assertThat(rateLimiter.tryConsume()).as("%d번째 요청", index).isTrue();
        }
        assertThat(rateLimiter.tryConsume()).as("%d번째 요청은 거부", rejectAt).isFalse();
    }

    @DisplayName("동시 요청 여러 개 중 capacity개만 통과합니다.")
    @Test
    void pass_only_capacity_under_concurrent_requests() throws InterruptedException {
        // Given: 시계를 고정해 보충을 차단하고, 동시에 여러 요청을 보내도록 준비합니다.
        int capacity = 3;
        int threadCount = 20;
        AtomicLong clock = new AtomicLong(0L);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger passed = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(threadCount);

        for (int index = 0; index < threadCount; index++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    if (rateLimiter.tryConsume()) {
                        passed.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // When: 모든 작업을 동시에 출발시킵니다.
        ready.await();
        start.countDown();
        executor.shutdown();

        // Then: 스레드가 경쟁해도 capacity 개수만 통과합니다.
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(passed.get()).isEqualTo(capacity);
    }
}
