package roomescape.payment.toss;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.domain.PaymentConfirmation;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TossPaymentGatewayTimeoutTest {

    private static final Duration READ_TIMEOUT = Duration.ofMillis(500);

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
    void 응답이_read_timeout을_넘기면_예외를_던진다() {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"paymentKey\":\"pk_1\",\"orderId\":\"order-1\",\"totalAmount\":50000}")
                .setBodyDelay(2, TimeUnit.SECONDS));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("pk_1", "order-1", 50_000L)))
                .isInstanceOf(RestClientException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
    }
}
