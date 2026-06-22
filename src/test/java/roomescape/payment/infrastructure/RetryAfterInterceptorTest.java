package roomescape.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentStatus;

class RetryAfterInterceptorTest {

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("test_pk_1", "order_1", 10000L, "idem-key-1");
    }

    private TossPaymentGateway gateway(int maxAttempts) {
        TossProperties properties =
                new TossProperties(mockWebServer.url("/").toString(), "", "test_gsk_dummy", 3000, 3000, maxAttempts);
        RestClient restClient = new TossClientConfig().tossRestClient(properties, 1000, 1000);
        return new TossPaymentGateway(restClient, new ObjectMapper());
    }

    private void enqueueTooManyRequests(String retryAfterSeconds) {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .addHeader("Retry-After", retryAfterSeconds)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"code\": \"EXCEED_RATE_LIMIT\", \"message\": \"요청이 많습니다\"}"));
    }

    private void enqueueSuccess() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {"paymentKey": "test_pk_1", "orderId": "order_1", "status": "DONE", "totalAmount": 10000}
                        """));
    }

    @Test
    void 토스가_429와_RetryAfter를_주면_그만큼_대기후_재시도해_최종_성공한다() throws InterruptedException {
        long before = mockWebServer.getRequestCount();
        enqueueTooManyRequests("1");
        enqueueSuccess();

        TossPaymentGateway gateway = gateway(3);

        long start = System.nanoTime();
        var result = gateway.confirm(confirmation());
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(mockWebServer.getRequestCount() - before).isEqualTo(2);
        assertThat(elapsedMs).isGreaterThanOrEqualTo(900);
    }

    @Test
    void maxAttempts를_다_써도_429면_도메인_예외로_실패한다() {
        long before = mockWebServer.getRequestCount();
        enqueueTooManyRequests("0");
        enqueueTooManyRequests("0");

        TossPaymentGateway gateway = gateway(2);

        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(TossPaymentException.class)
                .extracting(e -> ((TossPaymentException) e).getStatus())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(mockWebServer.getRequestCount() - before).isEqualTo(2);
    }
}
