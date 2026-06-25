package roomescape.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.dto.PaymentConfirmRequest;

class TossPaymentsClientTimeoutTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void 느린_응답은_read_timeout만큼만_기다리고_결과_확인_필요_예외를_던진다() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/payments/confirm", exchange -> {
            try {
                Thread.sleep(1_000);
                byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();

        TossPaymentsClient client = new TossPaymentsClient(
            RestClient.builder(),
            new ObjectMapper(),
            "http://localhost:" + server.getAddress().getPort(),
            "test_sk_dummy",
            Duration.ofMillis(100),
            Duration.ofMillis(150)
        );
        PaymentConfirmRequest request = new PaymentConfirmRequest("paymentKey", "orderId", 1000L);

        long startedAt = System.nanoTime();

        assertThatThrownBy(() -> client.confirm(request, "fixed-idempotency-key"))
            .isInstanceOf(PaymentResultUnknownException.class);

        Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
        assertThat(elapsed).isLessThan(Duration.ofSeconds(1));
    }

    @Test
    void 연결할_수_없는_서버는_연결_실패로_구분한다() {
        TossPaymentsClient client = new TossPaymentsClient(
            RestClient.builder(),
            new ObjectMapper(),
            "http://localhost:1",
            "test_sk_dummy",
            Duration.ofMillis(100),
            Duration.ofMillis(100)
        );
        PaymentConfirmRequest request = new PaymentConfirmRequest("paymentKey", "orderId", 1000L);

        assertThatThrownBy(() -> client.confirm(request, "fixed-idempotency-key"))
            .isInstanceOf(PaymentConnectionException.class);
    }
}
