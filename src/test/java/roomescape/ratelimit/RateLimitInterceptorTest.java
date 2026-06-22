package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.exception.ErrorCode;

class RateLimitInterceptorTest {

    @Test
    @DisplayName("한도를 초과한 요청은 컨트롤러를 호출하지 않고 429와 Retry-After를 반환한다.")
    void rejects_request_when_rate_limit_exceeded() throws Exception {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1, () -> 0L),
                new ObjectMapper()
        );

        assertThat(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()))
                .isTrue();

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean handled = interceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(handled).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
        assertThat(response.getContentAsString()).contains(ErrorCode.RATE_LIMIT_EXCEEDED.getCode());
    }
}
