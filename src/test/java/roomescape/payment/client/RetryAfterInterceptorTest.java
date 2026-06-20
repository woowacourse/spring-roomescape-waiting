package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
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

    @Test
    @DisplayName("429와 Retry-After를 받으면 대기 후 재시도해 최종 200을 받는다")
    void 게이트웨이가_429면_재시도해_최종_200을_받는다() throws IOException {
        MockClientHttpResponse tooMany = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        tooMany.getHeaders().add(HttpHeaders.RETRY_AFTER, "0");
        MockClientHttpResponse ok = new MockClientHttpResponse(
                "{\"status\":\"DONE\"}".getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
        StubExecution execution = new StubExecution(tooMany, ok);

        ClientHttpResponse response = new RetryAfterInterceptor(3).intercept(request(), new byte[0], execution);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        assertThat(execution.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Retry-After가 없으면 기본 1초 간격으로 폴백해 재시도한다")
    void RetryAfter가_없으면_1초_폴백으로_재시도한다() throws IOException {
        MockClientHttpResponse tooMany = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        MockClientHttpResponse ok = new MockClientHttpResponse(
                "{\"status\":\"DONE\"}".getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
        StubExecution execution = new StubExecution(tooMany, ok);

        long startNanos = System.nanoTime();
        ClientHttpResponse response = new RetryAfterInterceptor(3).intercept(request(), new byte[0], execution);
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        assertThat(execution.count()).isEqualTo(2);
        assertThat(elapsedMillis).isGreaterThanOrEqualTo(900L);
    }

    @Test
    @DisplayName("maxAttempts를 넘어도 429면 도메인 예외로 실패한다")
    void maxAttempts를_넘으면_도메인_예외로_실패한다() {
        MockClientHttpResponse first = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        first.getHeaders().add(HttpHeaders.RETRY_AFTER, "0");
        MockClientHttpResponse second = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        second.getHeaders().add(HttpHeaders.RETRY_AFTER, "0");
        StubExecution execution = new StubExecution(first, second);

        assertThatThrownBy(() -> new RetryAfterInterceptor(2).intercept(request(), new byte[0], execution))
                .isInstanceOf(TossPaymentException.GatewayBusy.class);
        assertThat(execution.count()).isEqualTo(2);
    }

    private MockClientHttpRequest request() {
        return new MockClientHttpRequest(HttpMethod.POST, URI.create("/v1/payments/confirm"));
    }

    private static final class StubExecution implements ClientHttpRequestExecution {

        private final Deque<ClientHttpResponse> responses = new ArrayDeque<>();
        private final AtomicInteger count = new AtomicInteger();

        private StubExecution(ClientHttpResponse... responses) {
            for (ClientHttpResponse response : responses) {
                this.responses.add(response);
            }
        }

        @Override
        public ClientHttpResponse execute(org.springframework.http.HttpRequest request, byte[] body) {
            count.incrementAndGet();
            return responses.poll();
        }

        private int count() {
            return count.get();
        }
    }
}