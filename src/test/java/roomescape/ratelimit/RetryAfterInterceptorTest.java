package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.infrastructure.payment.toss.toss.TossPaymentException;

class RetryAfterInterceptorTest {

    @Test
    void retry_after_초만큼_기다린_뒤_재시도한다() throws Exception {
        List<Duration> sleeps = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, Duration.ofSeconds(1), sleeps::add);
        AtomicInteger calls = new AtomicInteger();
        HttpRequest request = mock(HttpRequest.class);

        ClientHttpResponse response = interceptor.intercept(request, new byte[0], (req, body) -> {
            if (calls.incrementAndGet() == 1) {
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.RETRY_AFTER, "2");
                return new TestClientHttpResponse(HttpStatus.TOO_MANY_REQUESTS, headers);
            }
            return new TestClientHttpResponse(HttpStatus.OK, new HttpHeaders());
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(calls).hasValue(2);
        assertThat(sleeps).containsExactly(Duration.ofSeconds(2));
    }

    @Test
    void retry_after가_없으면_폴백_간격으로_재시도한다() throws Exception {
        List<Duration> sleeps = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, Duration.ofSeconds(1), sleeps::add);
        AtomicInteger calls = new AtomicInteger();
        HttpRequest request = mock(HttpRequest.class);

        ClientHttpResponse response = interceptor.intercept(request, new byte[0], (req, body) -> {
            if (calls.incrementAndGet() == 1) {
                return new TestClientHttpResponse(HttpStatus.TOO_MANY_REQUESTS, new HttpHeaders());
            }
            return new TestClientHttpResponse(HttpStatus.OK, new HttpHeaders());
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(calls).hasValue(2);
        assertThat(sleeps).containsExactly(Duration.ofSeconds(1));
    }

    @Test
    void 최대_시도를_넘어도_429이면_도메인_예외로_실패한다() {
        List<Duration> sleeps = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, Duration.ofSeconds(1), sleeps::add);
        AtomicInteger calls = new AtomicInteger();
        HttpRequest request = mock(HttpRequest.class);

        assertThatThrownBy(() -> interceptor.intercept(request, new byte[0], (req, body) -> {
            calls.incrementAndGet();
            return new TestClientHttpResponse(HttpStatus.TOO_MANY_REQUESTS, new HttpHeaders());
        })).isInstanceOf(TossPaymentException.RateLimited.class);

        assertThat(calls).hasValue(2);
        assertThat(sleeps).containsExactly(Duration.ofSeconds(1));
    }

    private static class TestClientHttpResponse implements ClientHttpResponse {

        private final HttpStatus status;
        private final HttpHeaders headers;

        private TestClientHttpResponse(HttpStatus status, HttpHeaders headers) {
            this.status = status;
            this.headers = headers;
        }

        @Override
        public HttpStatusCode getStatusCode() {
            return status;
        }

        @Override
        public String getStatusText() {
            return status.getReasonPhrase();
        }

        @Override
        public void close() {
        }

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
