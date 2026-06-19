package roomescape.infra.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketTest {

    @Test
    void 초기_토큰이_꽉_찬_상태에서_capacity만큼_연속_소비할_수_있다() {
        TokenBucket bucket = new TokenBucket(3, 1.0, fixedClock(0));

        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();
    }

    @Test
    void 경과_시간만큼_토큰이_보충된다() {
        long[] nanos = {0L};
        TokenBucket bucket = new TokenBucket(10, 2.0, () -> nanos[0]);

        // capacity 전부 소비
        for (int i = 0; i < 10; i++) {
            bucket.tryConsume();
        }
        assertThat(bucket.tryConsume()).isFalse();

        // 3초 경과 → 2.0 * 3 = 6개 보충
        nanos[0] = 3_000_000_000L;
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();
    }

    @Test
    void 보충량이_capacity를_초과하지_않는다() {
        long[] nanos = {0L};
        TokenBucket bucket = new TokenBucket(5, 10.0, () -> nanos[0]);

        for (int i = 0; i < 5; i++) {
            bucket.tryConsume();
        }

        // 100초 경과해도 최대 5개
        nanos[0] = 100_000_000_000L;

        int consumed = 0;
        while (bucket.tryConsume()) {
            consumed++;
        }
        assertThat(consumed).isEqualTo(5);
    }

    @Test
    void retryAfterSeconds_토큰이_없을때_보충에_필요한_초를_올림으로_반환한다() {
        long[] nanos = {0L};
        // refillPerSec=2: 1개 채우는 데 0.5초 → ceil → 1
        TokenBucket bucket = new TokenBucket(3, 2.0, () -> nanos[0]);

        for (int i = 0; i < 3; i++) {
            bucket.tryConsume();
        }

        assertThat(bucket.retryAfterSeconds()).isEqualTo(1L);
    }

    @Test
    void retryAfterSeconds_느린_보충률에서_올바른_대기시간을_반환한다() {
        long[] nanos = {0L};
        // refillPerSec=0.1: 1개 채우는 데 10초 → ceil → 10
        TokenBucket bucket = new TokenBucket(1, 0.1, () -> nanos[0]);

        bucket.tryConsume();

        assertThat(bucket.retryAfterSeconds()).isEqualTo(10L);
    }

    @Test
    void retryAfterSeconds_토큰이_있을때_0을_반환한다() {
        TokenBucket bucket = new TokenBucket(5, 1.0, fixedClock(0));

        assertThat(bucket.retryAfterSeconds()).isEqualTo(0L);
    }

    @Test
    void 동시_요청에서_정확히_capacity개만_통과한다() throws InterruptedException {
        int capacity = 10;
        int threadCount = 100;
        TokenBucket bucket = new TokenBucket(capacity, 0.0001, fixedClock(0));

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger passed = new AtomicInteger(0);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Thread t = Thread.ofVirtual().start(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (bucket.tryConsume()) {
                    passed.incrementAndGet();
                }
            });
            threads.add(t);
        }

        ready.await();
        start.countDown();
        for (Thread t : threads) {
            t.join();
        }

        assertThat(passed.get()).isEqualTo(capacity);
    }

    @Test
    void 동시_요청에서_capacity를_초과하지_않는다() throws InterruptedException {
        int capacity = 5;
        int threadCount = 200;
        TokenBucket bucket = new TokenBucket(capacity, 0.0, fixedClock(0));

        AtomicInteger passed = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    if (bucket.tryConsume()) {
                        passed.incrementAndGet();
                    }
                    latch.countDown();
                });
            }
            latch.await();
        }

        assertThat(passed.get()).isEqualTo(capacity);
    }

    private LongSupplier fixedClock(long nanos) {
        return () -> nanos;
    }
}