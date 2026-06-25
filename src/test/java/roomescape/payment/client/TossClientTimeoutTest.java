package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentTimeoutException;

class TossClientTimeoutTest {

    private static final int READ_TIMEOUT_MS = 500;
    private static final String BLACKHOLE_URL = "http://10.255.255.1:81";
    private static final String SUCCESS_BODY = """
            {"paymentKey": "test_pk_1", "orderId": "order-1", "status": "DONE", "totalAmount": 10000}
            """;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private TossPaymentGateway gateway(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(READ_TIMEOUT_MS));
        factory.setReadTimeout(Duration.ofMillis(READ_TIMEOUT_MS));
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic dGVzdDo=")
                .requestFactory(factory)
                .build();
        return new TossPaymentGateway(restClient, new ObjectMapper());
    }

    @Test
    void 읽기타임아웃이면_readTimeout만큼만_기다렸다가_PaymentTimeoutException으로_실패한다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY)
                .setHeadersDelay(2, TimeUnit.SECONDS));

        TossPaymentGateway gateway = gateway(mockWebServer.url("/").toString());

        long start = System.nanoTime();
        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("test_pk_1", "order-1", 10000L)))
                .isInstanceOf(PaymentTimeoutException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(elapsedMs).isLessThan(1500);
    }

    @Test
    void 연결자체가_실패하면_PaymentConnectionException으로_실패한다() {
        TossPaymentGateway gateway = gateway(BLACKHOLE_URL);

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("test_pk_1", "order-1", 10000L)))
                .isInstanceOf(PaymentConnectionException.class);
    }
}
