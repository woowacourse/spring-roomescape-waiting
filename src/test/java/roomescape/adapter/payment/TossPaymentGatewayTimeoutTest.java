package roomescape.adapter.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.exception.server.PaymentConnectionException;
import roomescape.exception.server.PaymentTimeoutException;
import roomescape.ratelimit.OutboundRateLimitProperties;

@SpringBootTest
class TossPaymentGatewayTimeoutTest {

    private static final String SUCCESS_BODY =
            "{\"paymentKey\":\"pk\",\"orderId\":\"order-1\",\"status\":\"DONE\",\"totalAmount\":10000}";

    static MockWebServer mockWebServer;

    static {
        mockWebServer = new MockWebServer();
        try {
            mockWebServer.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> mockWebServer.url("/").toString());
        registry.add("toss.secret-key", () -> "test_sk_dummy");
        registry.add("toss.client-key", () -> "test_ck_dummy");
        registry.add("toss.connect-timeout-ms", () -> "500");
        registry.add("toss.read-timeout-ms", () -> "500");   // 2초 지연을 빨리 끊기 위해 작게
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("test_pk_1", "order-1", 10000L);
    }

    @Test
    void 읽기_타임아웃은_PaymentTimeoutException으로_번역된다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY)
                .setHeadersDelay(2, TimeUnit.SECONDS));   // read timeout(500ms)이 먼저 끊는다

        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation()))
                .isInstanceOf(PaymentTimeoutException.class);   // 됐는지 모름 = 확인 필요
    }

    @Test
    void 연결_거부는_PaymentConnectionException으로_번역된다() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();   // 열었다 닫아 '아무도 안 듣는' 포트 확보 → 연결 거부
        }
        TossProperties props = new TossProperties(
                "http://localhost:" + closedPort, "test_sk_dummy", "test_ck_dummy", 500, 500, 3);
        OutboundRateLimitProperties outboundProps = new OutboundRateLimitProperties(10, 5);
        TossPaymentGateway gateway = new TossPaymentGateway(
                new TossClientConfig().tossRestClient(props, outboundProps), new ObjectMapper());

        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(PaymentConnectionException.class);   // 도달 못 함 = 안전
    }
}
