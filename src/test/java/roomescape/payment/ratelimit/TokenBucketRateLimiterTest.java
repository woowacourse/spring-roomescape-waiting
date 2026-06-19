package roomescape.payment.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * 토큰 버킷 로직을 가짜 시계(AtomicLong 주입)로 결정적으로 검증한다.
 */
class TokenBucketRateLimiterTest {

    @Test
    void capacity만큼_통과한뒤_거부되고_retryAfter는_보충시간이다() {
        var clock = new AtomicLong(0);
        var limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
        // 0개에서 초당 1개 보충 → 1개 차는 데 1초
        assertThat(limiter.retryAfterSeconds()).isEqualTo(1L);
    }

    @Test
    void N초_경과하면_토큰이_보충되어_다시_통과한다() {
        var clock = new AtomicLong(0);
        var limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);
        limiter.tryConsume();
        limiter.tryConsume();
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(1_000_000_000L); // +1초 → +1 토큰

        assertThat(limiter.tryConsume()).isTrue();
    }

    @Test
    void refillPerSec가_평균_TPS상한이_된다() {
        var clock = new AtomicLong(0);
        // refillPerSec=2.0 → 0.5초에 1개 보충(= 초당 2개 = TPS 상한 2). capacity=1 이라 버스트는 1.
        var limiter = new TokenBucketRateLimiter(1, 2.0, clock::get);
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(500_000_000L); // +0.5초 → 2.0 × 0.5 = 1개 보충

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 오래_기다려도_토큰은_capacity를_넘게_쌓이지_않는다() {
        var clock = new AtomicLong(0);
        var limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        clock.addAndGet(100_000_000_000L); // +100초

        // 버스트는 capacity(2)로 묶인다 → 2개만 통과, 3번째는 거부
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @ParameterizedTest(name = "capacity={0} → {1}번째 요청에서 거부")
    @CsvSource({"5, 6", "1, 2", "3, 4"})
    void 한도_파라미터에_따라_거부시점이_달라진다(long capacity, int rejectAt) {
        var clock = new AtomicLong(0);
        var limiter = new TokenBucketRateLimiter(capacity, 1.0, clock::get);

        for (var i = 1; i < rejectAt; i++) {
            assertThat(limiter.tryConsume()).as("%d번째 요청", i).isTrue();
        }
        assertThat(limiter.tryConsume()).as("%d번째 요청은 거부", rejectAt).isFalse();
    }

    @Test
    void 동시요청_여러개중_capacity개만_통과한다() throws InterruptedException {
        var capacity = 3;
        var threadCount = 20;
        var clock = new AtomicLong(0); // 고정 → 보충 없음
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
