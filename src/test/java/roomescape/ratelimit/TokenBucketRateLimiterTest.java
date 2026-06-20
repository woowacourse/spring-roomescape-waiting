package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import roomescape.global.web.ratelimit.TokenBucketRateLimiter;

class TokenBucketRateLimiterTest {

    private final AtomicLong clock = new AtomicLong();

    @Test
    void capacity만큼_통과한_뒤_거부하고_retryAfter를_계산한다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock::get);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();
        assertThat(limiter.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    void 시간이_지나면_토큰을_보충한다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, clock::get);

        assertThat(limiter.tryConsume()).isTrue();
        assertThat(limiter.tryConsume()).isFalse();

        clock.addAndGet(1_000_000_000L);

        assertThat(limiter.tryConsume()).isTrue();
    }
}
