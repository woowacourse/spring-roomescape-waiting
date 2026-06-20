package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    @Test
    @DisplayName("토큰이 있으면 통과(true)시키고 상태를 바꾸지 않는다")
    void 토큰이_있으면_통과한다() {
        AtomicLong clock = new AtomicLong(0);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(new TokenBucketRateLimiter(1, 0.001, clock::get));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("토큰이 없으면 거부(false)하고 429와 Retry-After 헤더를 세팅한다")
    void 토큰이_없으면_429와_RetryAfter를_세팅한다() {
        AtomicLong clock = new AtomicLong(0);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(new TokenBucketRateLimiter(1, 0.001, clock::get));
        MockHttpServletRequest request = new MockHttpServletRequest();
        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        MockHttpServletResponse rejected = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(request, rejected, new Object());

        assertThat(result).isFalse();
        assertThat(rejected.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(rejected.getHeader(HttpHeaders.RETRY_AFTER)).isNotNull();
    }
}