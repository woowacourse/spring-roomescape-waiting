package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    private final AtomicLong now = new AtomicLong();

    @Test
    void 토큰이_있으면_컨트롤러_호출을_허용한다() {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1.0, now::get));

        boolean result = interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        assertThat(result).isTrue();
    }

    @Test
    void 토큰이_없으면_429와_Retry_After를_응답하고_컨트롤러를_호출하지_않는다() {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1.0, now::get));
        interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
    }
}
