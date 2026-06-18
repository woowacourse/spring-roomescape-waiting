package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpResponse;

class RetryAfterInterceptorTest {

    @DisplayName("429 응답이면 Retry-After만큼 기다린 뒤 재시도한다.")
    @Test
    void retryAfterTooManyRequests() throws Exception {
        List<Duration> sleeps = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, Duration.ofSeconds(1), duration -> sleeps.add(duration));
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            int attempt = attempts.incrementAndGet();
            if (attempt == 1) {
                MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], 429);
                response.getHeaders().add(HttpHeaders.RETRY_AFTER, "2");
                return response;
            }
            return new MockClientHttpResponse("ok".getBytes(), 200);
        };

        var response = interceptor.intercept(request(), new byte[0], execution);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(attempts).hasValue(2);
        assertThat(sleeps).containsExactly(Duration.ofSeconds(2));
    }

    @DisplayName("Retry-After가 없으면 fallback delay를 사용하고 최대 시도 횟수를 넘으면 예외를 던진다.")
    @Test
    void failAfterMaxAttempts() {
        List<Duration> sleeps = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, Duration.ofMillis(100), duration -> sleeps.add(duration));
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            attempts.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], 429);
        };

        assertThatThrownBy(() -> interceptor.intercept(request(), new byte[0], execution))
                .isInstanceOf(RetryAfterExceededException.class);
        assertThat(attempts).hasValue(2);
        assertThat(sleeps).containsExactly(Duration.ofMillis(100));
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
