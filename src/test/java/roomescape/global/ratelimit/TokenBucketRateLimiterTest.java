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

/**
 * 토큰 버킷 로직을 가짜 시계(AtomicLong 주입)로 결정적으로 검증한다.
 */
class TokenBucketRateLimiterTest {

    @Test
    @DisplayName("capacity 만큼 통과한 뒤 거부되고, retryAfter 는 토큰 1개 보충에 필요한 시간이다")
    void capacity_만큼_통과한뒤_거부() {
        AtomicLong clock = new AtomicLong(0); // 고정 → 보충 없음
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
        // 0개에서 초당 1개 보충 → 1개 차는 데 1초
        assertThat(limiter.retryAfterSeconds()).isEqualTo(1L);
    }

    @Test
    @DisplayName("N초가 지나면 토큰이 보충되어 다시 통과한다")
    void 시간이_지나면_보충되어_다시_통과() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);
        limiter.tryConsume();
        limiter.tryConsume();
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(1_000_000_000L); // +1초 → +1 토큰

        assertThat(limiter.tryConsume()).isTrue();
    }

    @Test
    @DisplayName("refillPerSec 가 평균 TPS 상한이 된다")
    void refillPerSec가_평균_TPS상한() {
        AtomicLong clock = new AtomicLong(0);
        // refillPerSec=2.0 → 0.5초에 1개 보충(= 초당 2개). capacity=1 이라 버스트는 1.
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 2.0, clock::get);
        assertThat(limiter.tryConsume()).isTrue();  // 가득 찬 1개 소비
        assertThat(limiter.tryConsume()).isFalse(); // 비었다

        clock.addAndGet(500_000_000L); // +0.5초 → 2.0 × 0.5 = 1개 보충

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse(); // 0.5초당 1개 페이스라 곧바로 다시 빈다
    }

    @Test
    @DisplayName("오래 기다려도 토큰은 capacity 를 넘게 쌓이지 않는다")
    void 토큰은_capacity를_넘지_않는다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        clock.addAndGet(100_000_000_000L); // +100초: 상한이 없다면 토큰 100개가 쌓일 시간

        // 그래도 버스트는 capacity(2)로 묶인다 → 2개만 통과, 3번째는 거부
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @ParameterizedTest(name = "capacity={0} → {1}번째 요청에서 거부")
    @CsvSource({"5, 6", "1, 2", "3, 4"})
    @DisplayName("capacity 값에 따라 거부 시점이 달라진다")
    void 한도_파라미터에_따라_거부시점이_달라진다(long capacity, int rejectAt) {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);

        for (int i = 1; i < rejectAt; i++) {
            assertThat(limiter.tryConsume()).as("%d번째 요청", i).isTrue();
        }
        assertThat(limiter.tryConsume()).as("%d번째 요청은 거부", rejectAt).isFalse();
    }

    @Test
    @DisplayName("동시 요청이 몰려도 정확히 capacity 개만 통과한다")
    void 동시요청에서_capacity개만_통과() throws InterruptedException {
        int capacity = 3;
        int threadCount = 20;
        // 시계를 고정해 보충을 차단 → 동시 요청 중 정확히 capacity 개만 통과하는지(스레드 안전)를 본다.
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
                    start.await(); // 동시 출발
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
