package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.LongSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    @Test
    @DisplayName("토큰이 없으면 429와 Retry-After를 세팅하고 컨트롤러 호출을 막는다.")
    void rejectsWithRetryAfterWhenTokenIsEmpty() {
        FakeNanoClock clock = new FakeNanoClock();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1.0, clock);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(rateLimiter);

        assertThat(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()))
                .isTrue();

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
    }

    private static class FakeNanoClock implements LongSupplier {

        @Override
        public long getAsLong() {
            return 0;
        }
    }
}
