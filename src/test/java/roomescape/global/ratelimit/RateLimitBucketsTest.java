package roomescape.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RateLimitBucketsTest {

    private RateLimitBuckets rateLimitBuckets;

    @BeforeEach
    void setUp() {
        rateLimitBuckets = new RateLimitBuckets(10, 100.0, System::nanoTime);
    }

    @Nested
    class 버킷_생성_및_조회 {

        @Test
        void 동일한_키를_입력하면_동일한_버킷을_반환한다() {
            // given
            String key = "user-1";

            // when
            RateLimitBucket bucket1 = rateLimitBuckets.getOrCreateByKey(key);
            RateLimitBucket bucket2 = rateLimitBuckets.getOrCreateByKey(key);

            // then
            assertThat(bucket1).isEqualTo(bucket2);
        }

        @Test
        void 다른_키를_입력하면_서로_다른_버킷을_반환한다() {
            // given
            String key1 = "user-1";
            String key2 = "user-2";

            // when
            RateLimitBucket bucket1 = rateLimitBuckets.getOrCreateByKey(key1);
            RateLimitBucket bucket2 = rateLimitBuckets.getOrCreateByKey(key2);

            // then
            assertThat(bucket1).isNotEqualTo(bucket2);
        }
    }
}
