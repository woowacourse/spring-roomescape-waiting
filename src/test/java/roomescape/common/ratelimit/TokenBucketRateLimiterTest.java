package roomescape.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/**
 * 토큰 버킷을 가짜 시계(LongSupplier)로 결정적으로 검증한다. 실제 시간을 기다리지 않고 시계를 직접 전진시켜
 * 보충·상한·재시도 안내·동시성을 확인한다.
 */
class TokenBucketRateLimiterTest {

    @Test
    void 시작은_가득_차_있어_capacity개까지_통과하고_그_다음은_거부한다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1, clock::get);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 시간이_지나면_경과시간_곱하기_refillPerSec만큼_보충된다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 2, clock::get); // 초당 2개
        limiter.tryConsume();
        limiter.tryConsume();
        assertThat(limiter.tryConsume()).isFalse();

        clock.set(500_000_000L); // 0.5초 경과 → 1개 보충
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 보충은_capacity를_넘지_않는다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 100, clock::get);
        limiter.tryConsume();
        limiter.tryConsume();

        clock.set(10_000_000_000L); // 10초 → 1000개치 보충 가능하지만 capacity=2가 상한
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void retryAfterSeconds는_1개가_찰_때까지_필요한_초를_올림으로_반환한다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 2, clock::get); // 1개당 0.5초
        limiter.tryConsume(); // 비움

        assertThat(limiter.retryAfterSeconds()).isEqualTo(1L); // ceil(0.5) = 1
    }

    @Test
    void 토큰이_남아_있으면_retryAfterSeconds는_0이다() {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1, clock::get);

        assertThat(limiter.retryAfterSeconds()).isEqualTo(0L);
    }

    @Test
    void 동시_요청에서도_정확히_capacity개만_통과한다() throws InterruptedException {
        AtomicLong clock = new AtomicLong(0); // 시계 고정 → 보충 없음, 오직 capacity만큼만 통과해야 한다
        int capacity = 50;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1, clock::get);

        int threads = 200;
        // 풀이 작업 수보다 작으면 일부만 실행돼 배리어(ready)에 다 모이지 못하고 데드락 → 작업당 한 스레드.
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger passed = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (limiter.tryConsume()) {
                    passed.incrementAndGet();
                }
            });
        }
        ready.await();
        start.countDown(); // 모든 스레드를 동시에 풀어 경합을 만든다
        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        assertThat(passed.get()).isEqualTo(capacity);
    }
}
