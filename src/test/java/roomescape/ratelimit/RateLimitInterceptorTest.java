package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    @Test
    void 토큰이_있으면_통과시킨다() {
        var interceptor = new RateLimitInterceptor(new TokenBucketRateLimiter(1, 1.0, new AtomicLong(0)::get));
        var response = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(proceed).isTrue();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void 토큰이_없으면_429와_RetryAfter로_거부한다() {
        var limiter = new TokenBucketRateLimiter(1, 1.0, new AtomicLong(0)::get);
        var interceptor = new RateLimitInterceptor(limiter);
        limiter.tryConsume();

        var response = new MockHttpServletResponse();
        boolean proceed = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(proceed).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isNotNull();
    }
}
