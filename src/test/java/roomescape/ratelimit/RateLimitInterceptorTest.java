package roomescape.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitInterceptorTest {

    private final LongSupplier frozenClock = () -> 0L;

    @Test
    void 토큰이_있으면_preHandle이_true를_반환하고_컨트롤러로_진행시킨다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1, frozenClock);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(limiter);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 토큰이_없으면_컨트롤러를_호출하지_않고_429와_RetryAfter를_세팅한다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1, frozenClock);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(limiter);
        interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
    }
}
