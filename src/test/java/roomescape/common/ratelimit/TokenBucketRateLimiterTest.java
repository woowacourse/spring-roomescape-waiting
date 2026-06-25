package roomescape.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    private static final long SECOND = 1_000_000_000L;

    @Test
    void capacity만큼만_연속으로_통과시킨다() {
        // given : 가짜 시계 고정 (보충 없음)
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1, () -> 0L);

        // when & then
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void 시간이_지나면_refillPerSec만큼_보충된다() {
        // given : capacity 2, 초당 2개 보충
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 2, now::get);
        limiter.tryConsume();
        limiter.tryConsume();
        assertThat(limiter.tryConsume()).isFalse();

        // when : 1초 경과 → 2개 보충
        now.set(SECOND);

        // then
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
    }

    @Test
    void retryAfterSeconds는_1개가_찰_때까지_초를_올림한다() {
        // given : 초당 0.5개 보충 → 1개 차는 데 2초
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 0.5, () -> 0L);
        limiter.tryConsume();

        // when & then
        assertThat(limiter.retryAfterSeconds()).isEqualTo(2L);
    }

    @Test
    void 토큰이_남아있으면_retryAfter는_0이다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1, () -> 0L);

        assertThat(limiter.retryAfterSeconds()).isEqualTo(0L);
    }

    @Test
    void 동시_요청에서도_정확히_capacity개만_통과한다() throws Exception {
        // given
        int capacity = 50;
        int threadCount = 200;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1, () -> 0L);
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch start = new CountDownLatch(1);

        // when
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                start.await();
                return limiter.tryConsume();
            }));
        }
        start.countDown();

        long passed = 0;
        for (Future<Boolean> future : futures) {
            if (future.get(5, TimeUnit.SECONDS)) {
                passed++;
            }
        }
        executor.shutdown();

        // then
        assertThat(passed).isEqualTo(capacity);
    }
}
