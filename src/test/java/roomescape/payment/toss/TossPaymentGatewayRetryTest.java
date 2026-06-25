package roomescape.payment.toss;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TossPaymentGatewayRetryTest {

    private static final Duration READ_TIMEOUT = Duration.ofMillis(500);
    private static final String SUCCESS_BODY =
            "{\"paymentKey\":\"pk_1\",\"orderId\":\"order-1\",\"totalAmount\":50000}";

    private MockWebServer mockWebServer;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(READ_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        RestClient restClient = RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestFactory(requestFactory)
                .build();
        gateway = new TossPaymentGateway(restClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 첫_시도가_read_timeout이면_같은_멱등키로_재시도해_성공한다() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY)
                .setBodyDelay(2, TimeUnit.SECONDS));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY));

        PaymentResult result = gateway.confirm(new PaymentConfirmation("pk_1", "order-1", 50_000L));

        assertThat(result.paymentKey()).isEqualTo("pk_1");
        assertThat(result.orderId()).isEqualTo("order-1");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);

        RecordedRequest first = mockWebServer.takeRequest();
        RecordedRequest second = mockWebServer.takeRequest();
        assertThat(first.getHeader("Idempotency-Key"))
                .isNotBlank()
                .isEqualTo(second.getHeader("Idempotency-Key"));
    }

    @Test
    void 토스_오류_응답은_재시도하지_않고_즉시_예외를_던진다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"code\":\"INVALID_REQUEST\",\"message\":\"잘못된 요청입니다.\"}"));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("pk_1", "order-1", 50_000L)))
                .isInstanceOf(TossPaymentException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }
}
