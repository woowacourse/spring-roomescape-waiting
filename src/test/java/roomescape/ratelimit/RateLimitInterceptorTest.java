package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    @Test
    void 한도_내_요청은_통과시킨다() {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(1, 1);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(proceed).isTrue();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 초과_요청은_컨트롤러를_호출하지_않고_429와_RetryAfter로_거부한다() {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(1, 1);
        interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        MockHttpServletResponse denied = new MockHttpServletResponse();
        boolean proceed = interceptor.preHandle(new MockHttpServletRequest(), denied, new Object());

        assertThat(proceed).isFalse();
        assertThat(denied.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(denied.getHeader("Retry-After")).isEqualTo("1");
    }
}
