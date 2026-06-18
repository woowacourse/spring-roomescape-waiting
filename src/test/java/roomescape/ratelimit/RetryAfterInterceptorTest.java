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
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;

class RetryAfterInterceptorTest {

    @Test
    @DisplayName("토스가 429와 Retry-After를 반환하면 지정된 시간 대기 후 재시도한다.")
    void retries_after_retry_after_header_when_toss_returns_429() throws Exception {
        List<Duration> sleeps = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, Duration.ofMillis(10), sleeps::add);
        AtomicInteger attempts = new AtomicInteger();

        MockClientHttpResponse response = (MockClientHttpResponse) interceptor.intercept(
                request(),
                new byte[0],
                (request, body) -> {
                    if (attempts.incrementAndGet() == 1) {
                        MockClientHttpResponse retryResponse = new MockClientHttpResponse(
                                new byte[0],
                                HttpStatus.TOO_MANY_REQUESTS
                        );
                        retryResponse.getHeaders().set(HttpHeaders.RETRY_AFTER, "2");
                        return retryResponse;
                    }
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attempts).hasValue(2);
        assertThat(sleeps).containsExactly(Duration.ofSeconds(2));
    }

    @Test
    @DisplayName("Retry-After가 없으면 fallbackRetryAfter로 재시도한다.")
    void retries_with_fallback_when_retry_after_is_missing() throws Exception {
        List<Duration> sleeps = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, Duration.ofMillis(10), sleeps::add);
        AtomicInteger attempts = new AtomicInteger();

        interceptor.intercept(request(), new byte[0], (request, body) -> {
            if (attempts.incrementAndGet() == 1) {
                return new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
            }
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        });

        assertThat(attempts).hasValue(2);
        assertThat(sleeps).containsExactly(Duration.ofMillis(10));
    }

    @Test
    @DisplayName("maxAttempts 이후에도 429면 도메인 예외로 실패한다.")
    void fails_when_retry_attempts_are_exhausted() {
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, Duration.ofMillis(10), duration -> {
        });

        assertThatThrownBy(() -> interceptor.intercept(
                request(),
                new byte[0],
                (request, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS)
        ))
                .isInstanceOf(EscapeRoomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_GATEWAY_RATE_LIMITED);
    }

    private HttpRequest request() {
        return new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpMethod.POST;
            }

            @Override
            public URI getURI() {
                return URI.create("https://api.tosspayments.com/v1/payments/confirm");
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Idempotency-Key", "idempotency-key");
                return headers;
            }

            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }
        };
    }
}
