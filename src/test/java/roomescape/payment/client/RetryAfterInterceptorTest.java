package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

class RetryAfterInterceptorTest {

    @Test
    @DisplayName("429 응답의 Retry-After 초만큼 대기한 뒤 재시도한다.")
    void retriesAfterRetryAfterHeader() throws IOException {
        List<Long> waits = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, 1, waits::add);
        var responses = new ArrayDeque<MockClientHttpResponse>();
        responses.add(tooManyRequests("2"));
        responses.add(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        var response = interceptor.intercept(request(), new byte[0], (req, body) -> responses.remove());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(waits).containsExactly(2L);
    }

    @Test
    @DisplayName("Retry-After가 없으면 기본 대기 시간으로 재시도한다.")
    void retriesAfterFallbackSecondsWhenRetryAfterHeaderIsMissing() throws IOException {
        List<Long> waits = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, 1, waits::add);
        var responses = new ArrayDeque<MockClientHttpResponse>();
        responses.add(new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS));
        responses.add(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request(), new byte[0], (req, body) -> responses.remove());

        assertThat(waits).containsExactly(1L);
    }

    @Test
    @DisplayName("maxAttempts까지 429가 계속되면 도메인 예외로 실패한다.")
    void throwsDomainExceptionWhenAttemptsAreExhausted() {
        List<Long> waits = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, 1, waits::add);
        var responses = new ArrayDeque<MockClientHttpResponse>();
        responses.add(tooManyRequests("1"));
        responses.add(tooManyRequests("1"));

        assertThatThrownBy(() -> interceptor.intercept(request(), new byte[0], (req, body) -> responses.remove()))
                .isInstanceOf(TossPaymentException.RateLimited.class);
        assertThat(waits).containsExactly(1L);
    }

    private MockClientHttpResponse tooManyRequests(String retryAfter) {
        MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().set(HttpHeaders.RETRY_AFTER, retryAfter);
        return response;
    }

    private MockClientHttpRequest request() {
        return new MockClientHttpRequest(HttpMethod.POST, "https://api.tosspayments.com/v1/payments/confirm");
    }
}
