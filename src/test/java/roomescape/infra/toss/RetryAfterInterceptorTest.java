package roomescape.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

class RetryAfterInterceptorTest {

    private static final byte[] BODY = "{}".getBytes(StandardCharsets.UTF_8);

    @Test
    @DisplayName("429 응답에 Retry-After 헤더가 있으면 해당 초만큼 대기 후 재시도한다.")
    void retryAfterTooManyRequests() throws Exception {
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, 1);
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            if (attempts.incrementAndGet() == 1) {
                return tooManyRequests("0");
            }
            return new MockClientHttpResponse(successBody(), HttpStatus.OK);
        };

        ClientHttpResponse response = interceptor.intercept(request(), BODY, execution);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attempts).hasValue(2);
    }

    @Test
    @DisplayName("429 응답에 Retry-After 헤더가 없으면 기본 대기 시간으로 재시도한다.")
    void retryAfterFallbackSeconds() throws Exception {
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, 0);
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            if (attempts.incrementAndGet() == 1) {
                return tooManyRequests(null);
            }
            return new MockClientHttpResponse(successBody(), HttpStatus.OK);
        };

        ClientHttpResponse response = interceptor.intercept(request(), BODY, execution);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attempts).hasValue(2);
    }

    @Test
    @DisplayName("maxAttempts 이후에도 429이면 전용 예외로 실패한다.")
    void failAfterMaxAttempts() {
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, 1);
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            attempts.incrementAndGet();
            return tooManyRequests("0");
        };

        assertThatThrownBy(() -> interceptor.intercept(request(), BODY, execution))
                .isInstanceOf(TossPaymentException.TooManyRequests.class)
                .hasMessage("토스 요청 한도를 초과했습니다.");

        assertThat(attempts).hasValue(2);
    }

    private MockClientHttpRequest request() {
        return new MockClientHttpRequest(HttpMethod.POST, URI.create("/v1/payments/confirm"));
    }

    private MockClientHttpResponse tooManyRequests(String retryAfter) {
        MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        if (retryAfter != null) {
            response.getHeaders().set(HttpHeaders.RETRY_AFTER, retryAfter);
        }
        return response;
    }

    private byte[] successBody() {
        return """
                {
                  "paymentKey": "payment-key",
                  "orderId": "order-1",
                  "status": "DONE",
                  "totalAmount": 50000
                }
                """.getBytes(StandardCharsets.UTF_8);
    }
}
