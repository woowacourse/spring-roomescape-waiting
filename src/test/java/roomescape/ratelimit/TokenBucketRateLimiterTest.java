package roomescape.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketRateLimiterTest {

    @Test
    void capacity만큼만_초기_토큰이_있다() {
        long[] nanos = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1, () -> nanos[0]);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 경과_시간만큼_토큰이_보충된다() {
        long[] nanos = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 2, () -> nanos[0]);

        // 초기 토큰 5개 소진
        for (int i = 0; i < 5; i++) {
            limiter.tryConsume();
        }
        assertThat(limiter.tryConsume()).isFalse();

        // 1초 경과 → 2개 보충
        nanos[0] = 1_000_000_000L;
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 보충은_capacity를_넘지_않는다() {
        long[] nanos = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 10, () -> nanos[0]);

        // 초기 토큰 3개 소진
        for (int i = 0; i < 3; i++) {
            limiter.tryConsume();
        }

        // 100초 경과해도 capacity(3)까지만 보충
        nanos[0] = 100_000_000_000L;
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void retryAfterSeconds는_1개_보충까지_필요한_초를_올림_반환() {
        long[] nanos = {0L};
        // refillPerSec=2 이면 0.5초에 1개 보충 → 올림해서 1초
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 2, () -> nanos[0]);

        limiter.tryConsume(); // 토큰 소진
        assertThat(limiter.retryAfterSeconds()).isEqualTo(1L);
    }

    @Test
    void 동시_요청에서_capacity개만_통과한다() throws InterruptedException {
        int capacity = 5;
        int threads = 20;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1, System::nanoTime);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger passed = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            Thread.ofVirtual().start(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (limiter.tryConsume()) {
                    passed.incrementAndGet();
                }
            });
        }

        ready.await();
        start.countDown();
        Thread.sleep(200);

        assertThat(passed.get()).isEqualTo(capacity);
    }
}