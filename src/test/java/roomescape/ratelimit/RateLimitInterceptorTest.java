package roomescape.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitInterceptorTest {

    @Test
    void 토큰이_있으면_컨트롤러로_진행한다() {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1, System::nanoTime)
        );

        boolean result = interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        assertThat(result).isTrue();
    }

    @Test
    void 토큰이_없으면_429와_Retry_After로_거부하고_컨트롤러를_호출하지_않는다() {
        FakeNanoClock clock = new FakeNanoClock();
        RateLimitInterceptor interceptor = new RateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1, clock::now)
        );
        interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
    }

    private static class FakeNanoClock {

        private long now;

        long now() {
            return now;
        }
    }
}
