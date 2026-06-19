package roomescape.reservation.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.reservation.application.port.out.payment.PaymentConfirmation;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.application.port.out.payment.PaymentStatus;

@SpringBootTest
class TossClientIdempotencyTest {

    private static final MockWebServer mockWebServer = new MockWebServer();

    static {
        try {
            mockWebServer.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void overrideTossBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> mockWebServer.url("/").toString());
    }

    @AfterAll
    static void shutdown() throws IOException {
        mockWebServer.shutdown();
    }

    @DisplayName("read timeout 후 재시도해도 같은 멱등키면 중복 결제가 생기지 않습니다.")
    @Test
    void retry_after_read_timeout_does_not_create_duplicate_payment_with_same_idempotency_key() {
        IdempotentGatewayStub stub = new IdempotentGatewayStub();
        mockWebServer.setDispatcher(stub);

        PaymentConfirmation confirmation = paymentConfirmation();

        PaymentResult result = tossPaymentGateway.confirm(confirmation);

        assertThat(result.paymentKey()).isEqualTo("payment-key-1");
        assertThat(result.orderId()).isEqualTo("order-id");
        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(1_000L);
        assertThat(stub.createdPayments()).isEqualTo(1);
        assertThat(stub.seenKeys()).hasSize(2);
        assertThat(stub.seenKeys().get(0)).isEqualTo(stub.seenKeys().get(1));
        assertThat(stub.seenKeys().get(0)).isNotBlank();
    }

    private PaymentConfirmation paymentConfirmation() {
        return new PaymentConfirmation("payment-key", "order-id", 1_000L);
    }

    private static final class IdempotentGatewayStub extends Dispatcher {

        private final AtomicInteger paymentSequence = new AtomicInteger();
        private final Map<String, String> paymentsByIdempotencyKey = new ConcurrentHashMap<>();
        private final List<String> seenKeys = new CopyOnWriteArrayList<>();

        @Override
        public MockResponse dispatch(RecordedRequest request) {
            String idempotencyKey = request.getHeader("Idempotency-Key");
            seenKeys.add(idempotencyKey);

            String existingPaymentKey = idempotencyKey == null ? null : paymentsByIdempotencyKey.get(idempotencyKey);
            if (existingPaymentKey != null) {
                return success(existingPaymentKey);
            }

            String paymentKey = "payment-key-" + paymentSequence.incrementAndGet();
            if (idempotencyKey != null) {
                paymentsByIdempotencyKey.put(idempotencyKey, paymentKey);
            }
            return slowSuccess(paymentKey);
        }

        private MockResponse slowSuccess(String paymentKey) {
            return success(paymentKey)
                    .setBodyDelay(1, TimeUnit.SECONDS);
        }

        private MockResponse success(String paymentKey) {
            return new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("""
                            {
                              "paymentKey": "%s",
                              "orderId": "order-id",
                              "status": "DONE",
                              "totalAmount": 1000
                            }
                            """.formatted(paymentKey));
        }

        private int createdPayments() {
            return paymentSequence.get();
        }

        private List<String> seenKeys() {
            return seenKeys;
        }
    }
}
