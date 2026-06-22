package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    private static final long SEC = 1_000_000_000L;

    @Nested
    @DisplayName("tryConsume")
    class TryConsumeTest {

        @Test
        @DisplayName("초기 용량만큼 연속 소비 후 거부된다")
        void capacity개_소비_후_거부() {
            long[] now = {0L};
            TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 1.0, () -> now[0]);

            for (int i = 0; i < 5; i++) {
                assertThat(limiter.tryConsume()).isTrue();
            }
            assertThat(limiter.tryConsume()).isFalse();
        }

        @Test
        @DisplayName("토큰이 소진된 후 시간이 지나면 refillPerSec 비율로 보충된다")
        void 시간_경과_후_보충() {
            long[] now = {0L};
            TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 2.0, () -> now[0]);

            limiter.tryConsume();
            limiter.tryConsume();
            limiter.tryConsume();
            assertThat(limiter.tryConsume()).isFalse();

            now[0] = SEC / 2;
            assertThat(limiter.tryConsume()).isTrue();
            assertThat(limiter.tryConsume()).isFalse();
        }

        @Test
        @DisplayName("보충량이 capacity를 초과하지 않는다")
        void 보충은_capacity를_넘지_않는다() {
            long[] now = {0L};
            TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1.0, () -> now[0]);

            limiter.tryConsume();
            limiter.tryConsume();
            limiter.tryConsume();

            now[0] = 100 * SEC;
            assertThat(limiter.tryConsume()).isTrue();
            assertThat(limiter.tryConsume()).isTrue();
            assertThat(limiter.tryConsume()).isTrue();
            assertThat(limiter.tryConsume()).isFalse();
        }

        @Test
        @DisplayName("capacity가 1인 경우 한 번 소비하면 즉시 거부된다")
        void capacity_1이면_소비_후_즉시_거부() {
            long[] now = {0L};
            TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, () -> now[0]);

            assertThat(limiter.tryConsume()).isTrue();
            assertThat(limiter.tryConsume()).isFalse();
        }
    }

    @Nested
    @DisplayName("retryAfterSeconds")
    class RetryAfterSecondsTest {

        @Test
        @DisplayName("토큰이 있으면 0을 반환한다")
        void 토큰_있으면_0() {
            long[] now = {0L};
            TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 1.0, () -> now[0]);

            assertThat(limiter.retryAfterSeconds()).isEqualTo(0L);
        }

        @Test
        @DisplayName("토큰 소진 직후 — 1개 보충에 필요한 초를 올림으로 반환한다")
        void 토큰_소진_직후_올림() {
            long[] now = {0L};
            TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, () -> now[0]);

            limiter.tryConsume();
            now[0] = (long) (0.3 * SEC);
            assertThat(limiter.retryAfterSeconds()).isEqualTo(1L);
        }

        @Test
        @DisplayName("보충 속도가 느리면 더 긴 대기 시간을 반환한다")
        void 느린_보충_속도() {
            long[] now = {0L};
            TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 0.5, () -> now[0]);

            limiter.tryConsume();
            assertThat(limiter.retryAfterSeconds()).isEqualTo(2L);
        }

        @Test
        @DisplayName("부분 보충 후 나머지 대기 시간을 올림으로 반환한다")
        void 부분_보충_후_나머지_대기() {
            long[] now = {0L};
            TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, () -> now[0]);

            limiter.tryConsume();
            limiter.tryConsume();

            now[0] = (long) (0.7 * SEC);
            assertThat(limiter.retryAfterSeconds()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("생성자 유효성 검사")
    class ConstructorValidationTest {

        @Test
        @DisplayName("capacity가 0이면 예외")
        void capacity_0_예외() {
            assertThatThrownBy(() -> new TokenBucketRateLimiter(0, 1.0, () -> 0L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("refillPerSec가 0이면 예외")
        void refillPerSec_0_예외() {
            assertThatThrownBy(() -> new TokenBucketRateLimiter(1, 0.0, () -> 0L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("동시성")
    class ConcurrencyTest {

        @Test
        @DisplayName("N개 스레드가 동시에 접근해도 정확히 capacity개만 통과한다")
        void 동시_요청에서_정확히_capacity개만_통과() throws InterruptedException {
            int capacity = 10;
            int threads = 100;
            TokenBucketRateLimiter limiter =
                    new TokenBucketRateLimiter(capacity, 1.0, System::nanoTime);

            int passes = countConcurrentPasses(limiter, threads);

            assertThat(passes).isEqualTo(capacity);
        }

        private int countConcurrentPasses(TokenBucketRateLimiter limiter, int threads) throws InterruptedException {
            AtomicInteger passed = new AtomicInteger(0);
            CountDownLatch ready = new CountDownLatch(threads);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                Thread.ofVirtual().start(() -> {
                    ready.countDown();
                    awaitQuietly(ready);
                    if (limiter.tryConsume()) {
                        passed.incrementAndGet();
                    }
                    done.countDown();
                });
            }

            done.await(5, TimeUnit.SECONDS);
            return passed.get();
        }

        private void awaitQuietly(CountDownLatch latch) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
