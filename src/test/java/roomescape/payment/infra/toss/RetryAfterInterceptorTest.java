package roomescape.payment.infra.toss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RetryAfterInterceptorTest {

    @Test
    @DisplayName("429와 Retry-After를 받으면 해당 시간만큼 대기한 뒤 재시도한다.")
    void intercept_retriesAfterRetryAfter() throws Exception {
        List<Duration> delays = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, Duration.ofSeconds(1), delays::add);
        AtomicInteger executions = new AtomicInteger();

        var response = interceptor.intercept(request(), new byte[0], (request, body) -> {
            if (executions.incrementAndGet() == 1) {
                return tooManyRequests("2");
            }
            return ok();
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(executions.get()).isEqualTo(2);
        assertThat(delays).containsExactly(Duration.ofSeconds(2));
    }

    @Test
    @DisplayName("Retry-After가 없으면 폴백 간격으로 재시도한다.")
    void intercept_retriesAfterFallbackDelay() throws Exception {
        List<Duration> delays = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, Duration.ofSeconds(1), delays::add);
        AtomicInteger executions = new AtomicInteger();

        var response = interceptor.intercept(request(), new byte[0], (request, body) -> {
            if (executions.incrementAndGet() == 1) {
                return tooManyRequests(null);
            }
            return ok();
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(executions.get()).isEqualTo(2);
        assertThat(delays).containsExactly(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("maxAttempts에 도달하면 더 이상 재시도하지 않고 마지막 429를 반환한다.")
    void intercept_stopsAtMaxAttempts() throws Exception {
        List<Duration> delays = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, Duration.ZERO, delays::add);
        AtomicInteger executions = new AtomicInteger();

        var response = interceptor.intercept(request(), new byte[0], (request, body) -> {
            executions.incrementAndGet();
            return tooManyRequests("0");
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(executions.get()).isEqualTo(2);
        assertThat(delays).containsExactly(Duration.ZERO);
    }

    private MockClientHttpRequest request() {
        return new MockClientHttpRequest(HttpMethod.POST, URI.create("https://example.com/v1/payments/confirm"));
    }

    private MockClientHttpResponse ok() {
        return new MockClientHttpResponse(
                "{\"paymentKey\":\"payment-key\",\"orderId\":\"order-id\",\"totalAmount\":1000}".getBytes(StandardCharsets.UTF_8),
                HttpStatus.OK
        );
    }

    private MockClientHttpResponse tooManyRequests(String retryAfter) {
        MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        if (retryAfter != null) {
            response.getHeaders().set(HttpHeaders.RETRY_AFTER, retryAfter);
        }
        return response;
    }
}
