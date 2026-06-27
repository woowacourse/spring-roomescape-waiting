package roomescape.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.dto.PaymentConfirmRequest;
import roomescape.domain.payment.dto.PaymentConfirmResponse;
import roomescape.ratelimit.OutboundRateLimitInterceptor;
import roomescape.ratelimit.RetryAfterInterceptor;

class TossPaymentsClientRateLimitTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void 토스가_429를_반환하면_Retry_After_후_같은_멱등키로_재시도한다() throws IOException {
        AtomicInteger requestCount = new AtomicInteger();
        List<String> idempotencyKeys = new CopyOnWriteArrayList<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/payments/confirm", exchange -> {
            idempotencyKeys.add(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
            if (requestCount.incrementAndGet() == 1) {
                exchange.getResponseHeaders().add("Retry-After", "0");
                exchange.sendResponseHeaders(429, -1);
                exchange.close();
                return;
            }
            byte[] body = """
                {
                  "paymentKey": "paymentKey",
                  "orderId": "orderId",
                  "totalAmount": 1000,
                  "status": "DONE"
                }
                """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        TossPaymentsClient client = new TossPaymentsClient(
            RestClient.builder(),
            new ObjectMapper(),
            "http://localhost:" + server.getAddress().getPort(),
            "test_sk_dummy",
            Duration.ofSeconds(1),
            Duration.ofSeconds(1),
            new RetryAfterInterceptor(3, Duration.ZERO),
            new OutboundRateLimitInterceptor(10, 10)
        );

        PaymentConfirmResponse response = client.confirm(
            new PaymentConfirmRequest("paymentKey", "orderId", 1000L),
            "fixed-idempotency-key"
        );

        assertThat(response.status()).isEqualTo("DONE");
        assertThat(requestCount).hasValue(2);
        assertThat(idempotencyKeys).containsExactly(
            "fixed-idempotency-key",
            "fixed-idempotency-key"
        );
    }
}
