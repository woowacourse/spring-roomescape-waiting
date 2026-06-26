package roomescape.infrastructure.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

class RetryAfterInterceptorTest {

    @Test
    @DisplayName("429와 Retry-After를 받으면 지정된 시간만큼 대기 후 재시도한다")
    void intercept_retryAfter() throws IOException {
        List<Duration> sleeps = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, Duration.ofSeconds(1), sleeps::add);
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            if (attempts.incrementAndGet() == 1) {
                MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
                response.getHeaders().set(HttpHeaders.RETRY_AFTER, "2");
                return response;
            }
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        };

        ClientHttpResponse response = interceptor.intercept(mock(HttpRequest.class), new byte[0], execution);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attempts.get()).isEqualTo(2);
        assertThat(sleeps).containsExactly(Duration.ofSeconds(2));
    }

    @Test
    @DisplayName("Retry-After가 없으면 fallback 간격으로 재시도한다")
    void intercept_fallbackBackOff() throws IOException {
        List<Duration> sleeps = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, Duration.ofMillis(500), sleeps::add);
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            if (attempts.incrementAndGet() == 1) {
                return new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
            }
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        };

        ClientHttpResponse response = interceptor.intercept(mock(HttpRequest.class), new byte[0], execution);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sleeps).containsExactly(Duration.ofMillis(500));
    }

    @Test
    @DisplayName("maxAttempts를 넘어서도 429면 도메인 예외로 실패한다")
    void intercept_exceedsMaxAttempts() {
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, Duration.ofSeconds(1), duration -> {
        });
        ClientHttpRequestExecution execution = (request, body) ->
                new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);

        assertThatThrownBy(() -> interceptor.intercept(mock(HttpRequest.class), new byte[0], execution))
                .isInstanceOf(TossPaymentException.RateLimited.class);
    }
}
