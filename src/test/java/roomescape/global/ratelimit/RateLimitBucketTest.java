package roomescape.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RateLimitBucketTest {

    private static final int VALID_CAPACITY = 5;
    private static final double VALID_REFILL_PER_SECOND = 1.0;
    private static final long ONE_SECOND_IN_NANOS = 1_000_000_000L;

    @Nested
    class 생성_시점에_값을_검증한다 {

        @Test
        void 토큰의_최대_개수가_0_이하라면_예외를_던진다() {
            // given
            int invalidCapacity = 0;
            NanoClock mockClock = mock(NanoClock.class);

            // when and then
            assertThatThrownBy(() -> new RateLimitBucket(invalidCapacity, VALID_REFILL_PER_SECOND, mockClock))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("토큰의 최대 개수는 0보다 커야 합니다.");
        }

        @Test
        void 토큰의_보충_속도가_0_이하라면_예외를_던진다() {
            // given
            double invalidRefillPerSecond = 0.0;
            NanoClock mockClock = mock(NanoClock.class);

            // when and then
            assertThatThrownBy(() -> new RateLimitBucket(VALID_CAPACITY, invalidRefillPerSecond, mockClock))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("토큰의 보충 속도는 0보다 커야 합니다.");
        }
    }

    @Nested
    class 토큰을_소비한다 {

        @Test
        void 토큰이_충분할_경우_소비에_성공하고_true를_반환한다() {
            // given
            NanoClock mockClock = mock(NanoClock.class);
            when(mockClock.currentNanoseconds()).thenReturn(0L);

            RateLimitBucket bucket = new RateLimitBucket(VALID_CAPACITY, VALID_REFILL_PER_SECOND, mockClock);

            // when
            boolean isConsumed = bucket.tryConsume();

            // then
            assertThat(isConsumed).isTrue();
        }

        @Test
        void 토큰이_부족할_경우_소비에_실패하고_false를_반환한다() {
            // given
            NanoClock mockClock = mock(NanoClock.class);
            when(mockClock.currentNanoseconds()).thenReturn(0L);

            int capacityOne = 1;
            RateLimitBucket bucket = new RateLimitBucket(capacityOne, VALID_REFILL_PER_SECOND, mockClock);

            bucket.tryConsume(); // 준비된 토큰 소진

            // when
            boolean isConsumed = bucket.tryConsume();

            // then
            assertThat(isConsumed).isFalse();
        }

        @Test
        void 시간이_지나면_토큰이_보충되어_다시_소비할_수_있다() {
            // given
            NanoClock mockClock = mock(NanoClock.class);
            when(mockClock.currentNanoseconds()).thenReturn(0L);

            int capacityOne = 1;
            RateLimitBucket bucket = new RateLimitBucket(capacityOne, VALID_REFILL_PER_SECOND, mockClock);
            bucket.tryConsume(); // 준비된 토큰 소진

            when(mockClock.currentNanoseconds()).thenReturn(ONE_SECOND_IN_NANOS); // 모킹하는 맥락으로 개행

            // when
            boolean isConsumed = bucket.tryConsume();

            // then
            assertThat(isConsumed).isTrue();
        }

        @Test
        void 시간이_오래_지나도_토큰은_최대_개수를_초과하여_보충되지_않는다() {
            // given
            NanoClock mockClock = mock(NanoClock.class);
            when(mockClock.currentNanoseconds()).thenReturn(0L);

            RateLimitBucket bucket = new RateLimitBucket(VALID_CAPACITY, VALID_REFILL_PER_SECOND, mockClock);

            when(mockClock.currentNanoseconds()).thenReturn(ONE_SECOND_IN_NANOS * 10); // 10초 경과 (모킹하는 맥락으로 개행)

            // when
            for (int i = 0; i < VALID_CAPACITY; i++) {
                bucket.tryConsume(); // 최대치만큼 반복 소비
            }
            boolean isExceedinglyConsumed = bucket.tryConsume(); // 최대치 초과 소비 시도

            // then
            assertThat(isExceedinglyConsumed).isFalse();
        }
    }

    @Nested
    class 재시도_대기_시간을_계산한다 {

        @Test
        void 토큰이_남아있다면_0을_반환한다() {
            // given
            NanoClock mockClock = mock(NanoClock.class);
            when(mockClock.currentNanoseconds()).thenReturn(0L);

            RateLimitBucket bucket = new RateLimitBucket(VALID_CAPACITY, VALID_REFILL_PER_SECOND, mockClock);

            // when
            long retryAfter = bucket.retryAfterSeconds();

            // then
            assertThat(retryAfter).isZero();
        }

        @Test
        void 토큰이_부족하다면_대기해야_할_시간을_올림하여_초_단위로_반환한다() {
            // given
            NanoClock mockClock = mock(NanoClock.class);
            when(mockClock.currentNanoseconds()).thenReturn(0L);

            int capacityOne = 1;
            double refillHalfPerSecond = 0.5; // 1개를 채우는데 2초가 걸리는 속도
            RateLimitBucket bucket = new RateLimitBucket(capacityOne, refillHalfPerSecond, mockClock);

            bucket.tryConsume(); // 준비된 토큰 소진

            // when
            long retryAfter = bucket.retryAfterSeconds();

            // then
            assertThat(retryAfter).isEqualTo(2L);
        }
    }
}
