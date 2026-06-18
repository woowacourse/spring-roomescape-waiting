package roomescape.infrastructure.ratelimiter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketTest {

    @Test
    @DisplayName("초기 토큰은 capacity만큼 차 있어야 한다.")
    void initialTokens() {
        TokenBucket bucket = new TokenBucket(10, 2, () -> 0L);
        for (int i = 0; i < 10; i++) {
            assertThat(bucket.tryConsume()).isTrue();
        }
        assertThat(bucket.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("시간이 지남에 따라 토큰이 보충되어야 한다.")
    void refillTokens() {
        AtomicLong currentTime = new AtomicLong(0);
        TokenBucket bucket = new TokenBucket(10, 2, currentTime::get);

        // 모든 토큰 소비
        for (int i = 0; i < 10; i++) {
            bucket.tryConsume();
        }
        assertThat(bucket.tryConsume()).isFalse();

        // 1초 경과 (2개 보충)
        currentTime.addAndGet(1_000_000_000L);
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("보충되는 토큰은 capacity를 넘지 않아야 한다.")
    void refillCappedAtCapacity() {
        AtomicLong currentTime = new AtomicLong(0);
        TokenBucket bucket = new TokenBucket(10, 2, currentTime::get);

        // 100초 경과 (200개 보충 시도)
        currentTime.addAndGet(100L * 1_000_000_000L);
        
        for (int i = 0; i < 10; i++) {
            assertThat(bucket.tryConsume()).isTrue();
        }
        assertThat(bucket.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("retryAfterSeconds는 다음 토큰이 찰 때까지 남은 시간을 올림하여 반환한다.")
    void retryAfterSeconds() {
        AtomicLong currentTime = new AtomicLong(0);
        TokenBucket bucket = new TokenBucket(10, 1, currentTime::get); // 1 TPS

        // 모든 토큰 소비
        for (int i = 0; i < 10; i++) {
            bucket.tryConsume();
        }

        // 0.1초 경과 -> 토큰 0.1개. 0.9개 더 필요 -> 0.9초 필요 -> 올림하면 1초
        currentTime.addAndGet(100_000_000L);
        assertThat(bucket.retryAfterSeconds()).isEqualTo(1L);

        // 0.9초 더 경과 (총 1초) -> 토큰 1개. 0개 더 필요 -> 0초
        currentTime.addAndGet(900_000_000L);
        assertThat(bucket.retryAfterSeconds()).isEqualTo(0L);
    }

    @Test
    @DisplayName("동시 요청 시에도 정확히 capacity만큼만 통과해야 한다.")
    void concurrency() throws InterruptedException {
        int capacity = 50;
        TokenBucket bucket = new TokenBucket(capacity, 10, System::nanoTime);
        
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    latch.await();
                    if (bucket.tryConsume()) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS);

        assertThat(successCount.get()).isEqualTo(capacity);
    }
}
