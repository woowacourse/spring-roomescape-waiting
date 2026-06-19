package roomescape.infra.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitInterceptorTest {

    private static final Object HANDLER = new Object();

    @Test
    void 토큰이_있으면_true를_반환하고_응답에_영향을_주지_않는다() {
        TokenBucket bucket = new TokenBucket(1, 1.0, fixedClock(0));
        RateLimitInterceptor interceptor = new RateLimitInterceptor(bucket);

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, HANDLER);

        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Retry-After")).isNull();
    }

    @Test
    void 토큰이_없으면_false를_반환하고_429_상태코드를_설정한다() {
        TokenBucket bucket = new TokenBucket(1, 1.0, fixedClock(0));
        RateLimitInterceptor interceptor = new RateLimitInterceptor(bucket);
        bucket.tryConsume(); // 토큰 소진

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, HANDLER);

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    void 토큰이_없으면_Retry_After_헤더에_대기_시간을_담는다() {
        // refillPerSec=0.5 → 1개 채우는 데 2초 → ceil(2.0) = 2
        TokenBucket bucket = new TokenBucket(1, 0.5, fixedClock(0));
        RateLimitInterceptor interceptor = new RateLimitInterceptor(bucket);
        bucket.tryConsume();

        MockHttpServletResponse response = new MockHttpServletResponse();
        interceptor.preHandle(new MockHttpServletRequest(), response, HANDLER);

        assertThat(response.getHeader("Retry-After")).isEqualTo("2");
    }

    @Test
    void capacity만큼_연속_요청은_통과하고_이후_요청은_429를_받는다() {
        int capacity = 3;
        TokenBucket bucket = new TokenBucket(capacity, 1.0, fixedClock(0));
        RateLimitInterceptor interceptor = new RateLimitInterceptor(bucket);

        for (int i = 0; i < capacity; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            assertThat(interceptor.preHandle(new MockHttpServletRequest(), response, HANDLER)).isTrue();
            assertThat(response.getStatus()).isEqualTo(200);
        }

        MockHttpServletResponse blocked = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(new MockHttpServletRequest(), blocked, HANDLER)).isFalse();
        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isEqualTo("1");
    }

    private java.util.function.LongSupplier fixedClock(long nanos) {
        return () -> nanos;
    }
}