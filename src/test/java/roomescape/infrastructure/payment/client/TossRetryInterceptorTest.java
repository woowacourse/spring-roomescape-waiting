package roomescape.infrastructure.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import roomescape.exception.code.PaymentErrorCode;
import roomescape.exception.domain.PaymentException;

class TossRetryInterceptorTest {

    MockWebServer server;

    @BeforeEach
    void startServer() {
        server = new MockWebServer();
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    private RestClient clientWith(TossRetryInterceptor interceptor) {
        return RestClient.builder()
                .baseUrl(server.url("/").toString())
                .requestInterceptor(interceptor)
                .build();
    }

    private void enqueue(int status, String body) {
        server.enqueue(new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }

    private void enqueue(int status, String retryAfterSec, String body) {
        server.enqueue(new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setHeader("Retry-After", retryAfterSec)
                .setBody(body));
    }

    @Test
    @DisplayName("429가 아닌 응답(200)은 재시도 없이 그대로 반환한다")
    void 정상_200_응답_그대로_반환() {
        enqueue(200, "{\"result\":\"ok\"}");
        TossRetryInterceptor interceptor = new TossRetryInterceptor(3, 10L);

        var response = clientWith(interceptor).get().uri("/test")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Toss가 429+Retry-After를 주면 대기 후 재시도해 최종 200을 받는다")
    void 토스_429_RetryAfter_후_재시도_성공() {
        enqueue(429, "0", "{\"code\":\"RATE_LIMIT\",\"message\":\"too many requests\"}");
        enqueue(200, "{\"paymentKey\":\"pk1\",\"orderId\":\"order1\",\"status\":\"DONE\",\"totalAmount\":1000}");

        TossRetryInterceptor interceptor = new TossRetryInterceptor(3, 10L);

        var response = clientWith(interceptor).get().uri("/test")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Retry-After 헤더가 없으면 fallback 간격으로 재시도한다")
    void Retry_After_없으면_fallback_대기() {
        server.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"code\":\"RATE_LIMIT\",\"message\":\"too many\"}"));
        enqueue(200, "{\"result\":\"ok\"}");

        TossRetryInterceptor interceptor = new TossRetryInterceptor(3, 10L);

        var response = clientWith(interceptor).get().uri("/test")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("maxAttempts를 모두 소진해도 429면 PaymentException(TOSS_RATE_LIMIT_EXCEEDED)을 던진다")
    void maxAttempts_초과_시_도메인_예외() {
        enqueue(429, "0", "{\"code\":\"RATE_LIMIT\",\"message\":\"too many\"}");
        enqueue(429, "0", "{\"code\":\"RATE_LIMIT\",\"message\":\"too many\"}");
        enqueue(429, "0", "{\"code\":\"RATE_LIMIT\",\"message\":\"too many\"}");

        TossRetryInterceptor interceptor = new TossRetryInterceptor(3, 10L);
        RestClient client = clientWith(interceptor);

        assertThatThrownBy(() ->
                client.get().uri("/test").retrieve().toEntity(String.class)
        )
                .isInstanceOf(PaymentException.class)
                .satisfies(ex -> assertThat(((PaymentException) ex).getExceptionCode())
                        .isEqualTo(PaymentErrorCode.TOSS_RATE_LIMIT_EXCEEDED));

        assertThat(server.getRequestCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("maxAttempts=1이면 첫 번째 429에서 즉시 실패한다")
    void maxAttempts_1이면_첫_429에서_실패() {
        enqueue(429, "0", "{\"code\":\"RATE_LIMIT\",\"message\":\"too many\"}");

        TossRetryInterceptor interceptor = new TossRetryInterceptor(1, 10L);
        RestClient client = clientWith(interceptor);

        assertThatThrownBy(() ->
                client.get().uri("/test").retrieve().toEntity(String.class)
        )
                .isInstanceOf(PaymentException.class)
                .satisfies(ex -> assertThat(((PaymentException) ex).getExceptionCode())
                        .isEqualTo(PaymentErrorCode.TOSS_RATE_LIMIT_EXCEEDED));

        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("재시도 시 동일한 요청(Idempotency-Key 포함)이 전송된다")
    void 재시도_시_동일_요청_전송() throws InterruptedException {
        enqueue(429, "0", "{\"code\":\"RATE_LIMIT\",\"message\":\"too many\"}");
        enqueue(200, "{\"result\":\"ok\"}");

        TossRetryInterceptor interceptor = new TossRetryInterceptor(3, 10L);
        RestClient client = RestClient.builder()
                .baseUrl(server.url("/").toString())
                .defaultHeader("Idempotency-Key", "order-abc-123")
                .requestInterceptor(interceptor)
                .build();

        client.get().uri("/test").retrieve().toEntity(String.class);

        RecordedRequest first = server.takeRequest();
        RecordedRequest second = server.takeRequest();
        assertThat(first.getHeader("Idempotency-Key")).isEqualTo("order-abc-123");
        assertThat(second.getHeader("Idempotency-Key")).isEqualTo("order-abc-123");
    }
}
