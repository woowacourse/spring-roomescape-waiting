package roomescape.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RateLimitersTest {

    private static final long ONE_SECOND_IN_NANOS = 1_000_000_000L;

    /**
     * 시간을 임의로 진행시킬 수 있는 가짜 시계. 토큰 보충을 결정적으로 검증하기 위해 사용한다.
     */
    private static class FakeNanoClock implements NanoClock {

        private long now = 0L;

        @Override
        public long currentNanoseconds() {
            return now;
        }

        void advanceSeconds(long seconds) {
            now += seconds * ONE_SECOND_IN_NANOS;
        }
    }

    private RateLimiters rateLimitersWith(RateLimitType type, int capacity, double refillPerSecond, NanoClock clock) {
        RateLimitProperties properties = new RateLimitProperties();
        properties.getLimits().put(type, new RateLimitProperties.Limit(capacity, refillPerSecond));
        return new RateLimiters(properties, clock);
    }

    @Nested
    class 종류별로_독립된_한도를_적용한다 {

        @Test
        void 인바운드와_아웃바운드는_같은_키여도_서로_다른_시점에_거부된다() {
            // given: 같은 키를 쓰되 인바운드 허용량 2, 아웃바운드 허용량 1
            FakeNanoClock clock = new FakeNanoClock();
            RateLimitProperties properties = new RateLimitProperties();
            properties.getLimits().put(RateLimitType.INBOUND, new RateLimitProperties.Limit(2, 1.0));
            properties.getLimits().put(RateLimitType.OUTBOUND, new RateLimitProperties.Limit(1, 1.0));
            RateLimiters rateLimiters = new RateLimiters(properties, clock);

            String key = "shared-key";

            // then: 아웃바운드는 1개만 통과하고 두 번째에 거부된다
            assertThat(rateLimiters.getBucket(RateLimitType.OUTBOUND, key).tryConsume()).isTrue();
            assertThat(rateLimiters.getBucket(RateLimitType.OUTBOUND, key).tryConsume()).isFalse();

            // and: 같은 시점·같은 키여도 인바운드는 독립적으로 2개까지 통과한다
            assertThat(rateLimiters.getBucket(RateLimitType.INBOUND, key).tryConsume()).isTrue();
            assertThat(rateLimiters.getBucket(RateLimitType.INBOUND, key).tryConsume()).isTrue();
            assertThat(rateLimiters.getBucket(RateLimitType.INBOUND, key).tryConsume()).isFalse();
        }
    }

    @Nested
    class 토큰이_보충되면_다시_통과한다 {

        @Test
        void 아웃바운드_한도를_초과하면_거부되고_시간이_지나_보충되면_다시_통과한다() {
            // given: 아웃바운드 허용량 1, 초당 1개 보충
            FakeNanoClock clock = new FakeNanoClock();
            RateLimiters rateLimiters = rateLimitersWith(RateLimitType.OUTBOUND, 1, 1.0, clock);
            String key = "payment_outbound";

            // when: 첫 호출은 통과, 두 번째는 한도 초과로 거부
            assertThat(rateLimiters.getBucket(RateLimitType.OUTBOUND, key).tryConsume()).isTrue();
            assertThat(rateLimiters.getBucket(RateLimitType.OUTBOUND, key).tryConsume()).isFalse();

            // and: 1초가 지나 토큰이 보충되면
            clock.advanceSeconds(1);

            // then: 다시 통과한다
            assertThat(rateLimiters.getBucket(RateLimitType.OUTBOUND, key).tryConsume()).isTrue();
        }
    }

    @Nested
    class 설정이_없으면_종류별_기본값으로_폴백한다 {

        @Test
        void 오버라이드가_없으면_RateLimitType의_기본_허용량을_사용한다() {
            // given: 아무 설정도 주지 않음
            FakeNanoClock clock = new FakeNanoClock();
            RateLimiters rateLimiters = new RateLimiters(new RateLimitProperties(), clock);
            String key = "any";

            // then: OUTBOUND 기본 허용량(5)만큼 통과한 뒤 거부된다
            for (int i = 0; i < RateLimitType.OUTBOUND.defaultCapacity(); i++) {
                assertThat(rateLimiters.getBucket(RateLimitType.OUTBOUND, key).tryConsume()).isTrue();
            }
            assertThat(rateLimiters.getBucket(RateLimitType.OUTBOUND, key).tryConsume()).isFalse();
        }
    }
}
