package roomescape.global.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.global.web.ratelimit.RateLimitInterceptor;
import roomescape.global.web.ratelimit.TokenBucketRateLimiter;

class RateLimitInterceptorTest {

    @Test
    void 한도_초과_요청은_429와_RetryAfter로_거부한다() {
        AtomicLong clock = new AtomicLong();
        RateLimitInterceptor interceptor = new RateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1.0, clock::get)
        );

        assertThat(interceptor.preHandle(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new Object()
        )).isTrue();

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(new MockHttpServletRequest(), response, new Object())).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
    }
}
