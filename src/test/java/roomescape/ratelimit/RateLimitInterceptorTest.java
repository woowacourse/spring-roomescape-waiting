package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    @Test
    void 토큰이_있으면_컨트롤러로_요청을_넘긴다() throws Exception {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1.0, clock::get);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(rateLimiter);

        boolean result = interceptor.preHandle(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new Object()
        );

        assertThat(result).isTrue();
    }

    @Test
    void 토큰이_없으면_429와_Retry_After를_반환하고_컨트롤러를_호출하지_않는다() throws Exception {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1.0, clock::get);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(rateLimiter);
        interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
        assertThat(response.getContentAsString()).contains("요청이 너무 많습니다.");
    }
}
