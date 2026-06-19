package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpResponse;

class OutboundRateLimitInterceptorTest {

    @DisplayName("나가는 토큰이 없으면 외부 호출을 보내지 않는다.")
    @Test
    void rejectBeforeExternalCall() throws Exception {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1, () -> 0L);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(rateLimiter);
        AtomicInteger executionCount = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            executionCount.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], 200);
        };

        assertThat(interceptor.intercept(request(), new byte[0], execution)).isNotNull();
        assertThatThrownBy(() -> interceptor.intercept(request(), new byte[0], execution))
                .isInstanceOf(OutboundRateLimitException.class);
        assertThat(executionCount).hasValue(1);
    }

    @DisplayName("토큰이 보충되면 나가는 호출을 다시 보낸다.")
    @Test
    void allowAfterRefill() throws Exception {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1, now::get);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(rateLimiter);
        AtomicInteger executionCount = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            executionCount.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], 200);
        };

        interceptor.intercept(request(), new byte[0], execution);
        assertThatThrownBy(() -> interceptor.intercept(request(), new byte[0], execution))
                .isInstanceOf(OutboundRateLimitException.class);

        now.addAndGet(1_000_000_000L);
        interceptor.intercept(request(), new byte[0], execution);

        assertThat(executionCount).hasValue(2);
    }

    private HttpRequest request() {
        return new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpMethod.POST;
            }

            @Override
            public URI getURI() {
                return URI.create("https://example.com");
            }

            @Override
            public org.springframework.http.HttpHeaders getHeaders() {
                return new org.springframework.http.HttpHeaders();
            }

            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }
        };
    }
}
