package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    @Test
    void 토큰이_없으면_429와_Retry_After를_응답하고_컨트롤러_호출을_막는다() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 0.5, () -> 0L);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(limiter);
        limiter.tryConsume();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(
                new MockHttpServletRequest(),
                response,
                new Object()
        );

        assertThat(proceed).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("2");
    }
}
