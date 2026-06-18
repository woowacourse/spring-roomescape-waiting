package roomescape.pg.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.payment.PaymentStatus;
import roomescape.pg.PaymentConfirmation;
import roomescape.pg.PaymentGatewayResult;

@SpringBootTest
class TossClientIdempotencyTest {

    private static final IdempotentGatewayStub STUB = new IdempotentGatewayStub();
    private static final HttpServer SERVER = startServer();

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> "http://localhost:" + SERVER.getAddress().getPort());
        registry.add("toss.secret-key", () -> "test_gsk_dummy");
        registry.add("toss.connect-timeout-ms", () -> "500");
        registry.add("toss.read-timeout-ms", () -> "500");
    }

    @BeforeEach
    void setUp() {
        STUB.reset();
    }

    @AfterAll
    static void tearDown() {
        SERVER.stop(0);
    }

    @Test
    void read_timeout_후_같은_멱등키로_재시도하면_중복_승인하지_않는다() {
        PaymentConfirmation confirmation = new PaymentConfirmation(
                "test_pk_1",
                "order-1",
                10000L
        );

        assertThat(tossPaymentGateway.confirm(confirmation))
                .isInstanceOf(PaymentGatewayResult.Unknown.class);

        var result = tossPaymentGateway.confirm(confirmation);

        assertThat(result)
                .isInstanceOfSatisfying(PaymentGatewayResult.Approved.class, approved ->
                        assertThat(approved.payment().status()).isEqualTo(PaymentStatus.DONE)
                );
        assertThat(STUB.createdPayments()).isEqualTo(1);
        assertThat(STUB.seenKeys()).containsExactly("order-1", "order-1");
    }

    private static HttpServer startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/v1/payments/confirm", STUB::handle);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            return server;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final class IdempotentGatewayStub {

        private final Map<String, String> paymentKeyByIdempotencyKey = new ConcurrentHashMap<>();
        private final List<String> seenKeys = new CopyOnWriteArrayList<>();
        private final AtomicInteger createdPayments = new AtomicInteger();

        void reset() {
            paymentKeyByIdempotencyKey.clear();
            seenKeys.clear();
            createdPayments.set(0);
        }

        void handle(HttpExchange exchange) throws IOException {
            String idempotencyKey = exchange.getRequestHeaders().getFirst("Idempotency-Key");
            seenKeys.add(idempotencyKey);

            String paymentKey = paymentKeyByIdempotencyKey.get(idempotencyKey);
            if (paymentKey == null) {
                paymentKey = "pk_" + createdPayments.incrementAndGet();
                paymentKeyByIdempotencyKey.put(idempotencyKey, paymentKey);
                sleepLongerThanReadTimeout();
            }

            byte[] body = """
                    {"paymentKey":"%s","orderId":"order-1","status":"DONE","totalAmount":10000}
                    """.formatted(paymentKey).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        }

        int createdPayments() {
            return createdPayments.get();
        }

        List<String> seenKeys() {
            return seenKeys;
        }

        private void sleepLongerThanReadTimeout() {
            try {
                Thread.sleep(1_200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
