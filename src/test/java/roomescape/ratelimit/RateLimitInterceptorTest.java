package roomescape.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitInterceptorTest {

    @Test
    @DisplayName("토큰이 있으면 통과(true)시킨다.")
    void allowWithinLimit() {
        RateLimitInterceptor interceptor = interceptor(1, 1);

        boolean result = interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("토큰이 없으면 컨트롤러를 호출하지 않고 429와 Retry-After를 응답한다.")
    void rejectOverLimit() {
        RateLimitInterceptor interceptor = interceptor(1, 2);
        interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
    }

    private RateLimitInterceptor interceptor(long capacity, double refillPerSec) {
        AtomicLong fixedClock = new AtomicLong(0);
        return new RateLimitInterceptor(new TokenBucketRateLimiter(capacity, refillPerSec, fixedClock::get));
    }
}
