package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    @Test
    void 한도를_초과하면_429와_Retry_After를_반환한다() {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(
            new TokenBucketRateLimiter(1, 1, () -> 0L)
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        boolean firstResult = interceptor.preHandle(request, firstResponse, new Object());
        boolean secondResult = interceptor.preHandle(request, secondResponse, new Object());

        assertThat(firstResult).isTrue();
        assertThat(secondResult).isFalse();
        assertThat(secondResponse.getStatus()).isEqualTo(429);
        assertThat(secondResponse.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
    }
}
