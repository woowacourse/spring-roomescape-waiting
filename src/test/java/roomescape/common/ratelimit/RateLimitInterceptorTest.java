package roomescape.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 들어오는 Rate Limit 인터셉터가 토큰 유무에 따라 통과/거부(429+Retry-After)를 올바로 내는지 검증한다.
 */
class RateLimitInterceptorTest {

    @Test
    void 토큰이_있으면_통과시키고_응답에_손대지_않는다() {
        AtomicLong clock = new AtomicLong(0);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1, clock::get));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value()); // 기본 200 유지
    }

    @Test
    void 토큰이_없으면_컨트롤러를_막고_429와_Retry_After로_거부한다() {
        AtomicLong clock = new AtomicLong(0);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1, clock::get));
        // 첫 요청이 유일한 토큰을 소비한다.
        interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
    }
}
