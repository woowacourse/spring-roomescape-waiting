package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    @DisplayName("토큰이 있으면 요청을 통과시킨다.")
    @Test
    void allowWithinLimit() throws Exception {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1, () -> 0L);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(rateLimiter);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isTrue();
        assertThat(response.isCommitted()).isFalse();
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isNull();
    }

    @DisplayName("토큰이 없으면 429와 Retry-After를 반환하고 컨트롤러 호출을 막는다.")
    @Test
    void rejectTooManyRequests() throws Exception {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1, () -> 0L);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(rateLimiter);
        interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
        assertThat(response.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
    }
}
