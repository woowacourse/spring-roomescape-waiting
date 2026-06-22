package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    private static final long SEC = 1_000_000_000L;

    @Test
    @DisplayName("토큰이 있으면 preHandle이 true를 반환하고 응답을 건드리지 않는다")
    void 토큰_있으면_통과() throws Exception {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 1.0, () -> now[0]);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(limiter);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/payments/success");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("토큰 소진 시 preHandle이 false를 반환하고 429를 세팅한다")
    void 토큰_소진_시_429() throws Exception {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, () -> now[0]);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(limiter);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/reservations");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("429 응답에 Retry-After 헤더가 포함된다")
    void 한도초과_응답에_Retry_After_헤더() throws Exception {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, () -> now[0]);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(limiter);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reservations/mine");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        now[0] = (long) (0.3 * SEC);
        interceptor.preHandle(request, response, new Object());

        assertThat(response.getHeader("Retry-After")).isEqualTo("1");
    }

    @Test
    @DisplayName("한도 초과 시 preHandle이 false를 반환해 DispatcherServlet이 컨트롤러를 호출하지 않는다")
    void 한도_초과_시_컨트롤러_미호출() throws Exception {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, () -> now[0]);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(limiter);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/payments/success");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean firstResult = interceptor.preHandle(request, response, new Object());
        boolean secondResult = interceptor.preHandle(request, response, new Object());

        assertThat(firstResult).isTrue();
        assertThat(secondResult).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("429 응답 본문에 exceptionCode와 message가 포함된다")
    void 한도초과_응답_본문_형식() throws Exception {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, () -> now[0]);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(limiter);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/reservations");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        interceptor.preHandle(request, response, new Object());

        String body = response.getContentAsString();
        assertThat(body).contains("RATE_LIMIT_EXCEEDED");
        assertThat(body).contains("요청 한도를 초과했습니다");
        assertThat(response.getContentType()).contains("application/json");
    }

    @Test
    @DisplayName("capacity개 요청은 모두 통과하고, 그 다음 요청부터 거부된다")
    void capacity개_통과_후_거부() throws Exception {
        int capacity = 3;
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1.0, () -> now[0]);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(limiter);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/reservations");

        for (int i = 0; i < capacity; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            boolean result = interceptor.preHandle(request, response, new Object());
            assertThat(result).as("요청 %d번 통과해야 함", i + 1).isTrue();
        }

        MockHttpServletResponse rejected = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(request, rejected, new Object());
        assertThat(result).isFalse();
        assertThat(rejected.getStatus()).isEqualTo(429);
    }
}
