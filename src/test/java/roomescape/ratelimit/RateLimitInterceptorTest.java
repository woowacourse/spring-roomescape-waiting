package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    @Test
    void 한도_초과_요청은_429와_retry_after로_거부한다() {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1, () -> 0L);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(rateLimiter);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();

        MockHttpServletResponse rejectedResponse = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(request, rejectedResponse, new Object());

        assertThat(result).isFalse();
        assertThat(rejectedResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(rejectedResponse.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
    }
}
