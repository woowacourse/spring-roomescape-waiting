package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class TokenBucketTest {

    @Test
    void refillsDeterministicallyFromInjectedNanoTimeTest() {
        AtomicLong now = new AtomicLong(0L);
        TokenBucket bucket = new TokenBucket(1, 2D, now::get);

        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();
        assertThat(bucket.retryAfterSeconds()).isEqualTo(1L);

        now.addAndGet(500_000_000L);

        assertThat(bucket.tryConsume()).isTrue();
    }

    @Test
    void consumesOnlyCapacityUnderConcurrencyTest() throws InterruptedException {
        AtomicLong now = new AtomicLong(0L);
        TokenBucket bucket = new TokenBucket(3, 1D, now::get);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch ready = new CountDownLatch(8);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger allowed = new AtomicInteger();
        List<Throwable> failures = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    if (bucket.tryConsume()) {
                        allowed.incrementAndGet();
                    }
                } catch (Throwable e) {
                    synchronized (failures) {
                        failures.add(e);
                    }
                }
            });
        }

        assertThat(ready.await(1, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();

        assertThat(failures).isEmpty();
        assertThat(allowed.get()).isEqualTo(3);
    }
}
