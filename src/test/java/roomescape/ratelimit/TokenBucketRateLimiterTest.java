package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    @Test
    void capacity만큼_즉시_소비하고_이후에는_거부한다() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1, now::get);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    void 시간이_지나면_refillPerSecond만큼_토큰이_채워진다() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1, now::get);

        rateLimiter.tryConsume();
        rateLimiter.tryConsume();
        now.addAndGet(1_000_000_000L);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void capacity를_넘어서_보충되지_않는다() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 10, now::get);

        rateLimiter.tryConsume();
        now.addAndGet(10_000_000_000L);

        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isTrue();
        assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    void 토큰이_일부만_보충되면_남은_대기시간을_올림해서_반환한다() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 2, now::get);

        rateLimiter.tryConsume();
        now.addAndGet(250_000_000L);

        assertThat(rateLimiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    void 토큰이_있으면_retryAfterSeconds는_0이다() {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1, now::get);

        assertThat(rateLimiter.retryAfterSeconds()).isZero();
    }

    @Test
    void 동시에_요청해도_capacity만큼만_성공한다() throws InterruptedException {
        int capacity = 10;
        int threadCount = 50;
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(capacity, 1, new AtomicLong(0)::get);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (rateLimiter.tryConsume()) {
                    successCount.incrementAndGet();
                }
            });
            threads.add(thread);
            thread.start();
        }

        ready.await();
        start.countDown();
        for (Thread thread : threads) {
            thread.join();
        }

        assertThat(successCount.get()).isEqualTo(capacity);
    }
}
