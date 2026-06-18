package roomescape.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentStatus;

/**
 * read timeout 은 "요청은 도달했는데 응답만 못 받은" 상태를 만들 수 있어, 끊긴 뒤 재시도하면 같은 결제가
 * 두 번 일어날 수 있다. Idempotency-Key 로 그 중복을 막는지 확인한다.
 */
class TossClientIdempotencyTest {

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
        return new PaymentConfirmation("test_pk_1", "order-1", 10000L, "idem-key-1");
    }

    private TossPaymentGateway gateway() {
        TossProperties properties = new TossProperties(mockWebServer.url("/").toString(), "", "test_gsk_dummy", 500, 500);
        return new TossPaymentGateway(new TossClientConfig().tossRestClient(properties), new ObjectMapper());
    }

    @Test
    void read_timeout으로_끊겨_재시도해도_같은_멱등키면_중복_결제가_생기지_않는다() {
        IdempotentGatewayStub stub = new IdempotentGatewayStub();
        mockWebServer.setDispatcher(stub);
        TossPaymentGateway gateway = gateway();
        PaymentConfirmation confirmation = confirmation();

        // 1차: 서버는 결제를 처리하지만 응답이 느려 read timeout(500ms)으로 끊긴다.
        assertThatThrownBy(() -> gateway.confirm(confirmation))
                .isInstanceOf(RestClientException.class);

        // 2차(재시도): 같은 Idempotency-Key 라 서버가 중복을 인지해 재처리 없이 첫 결과를 즉시 돌려준다.
        var result = gateway.confirm(confirmation);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(stub.createdPayments()).isEqualTo(1);
        assertThat(stub.seenKeys()).hasSize(2);
        assertThat(stub.seenKeys().get(0)).isNotBlank().isEqualTo(stub.seenKeys().get(1));
    }

    private static class IdempotentGatewayStub extends Dispatcher {

        private final Map<String, String> paymentKeyByIdempotencyKey = new ConcurrentHashMap<>();
        private final AtomicInteger createdPayments = new AtomicInteger();
        private final List<String> seenKeys = new CopyOnWriteArrayList<>();

        @Override
        public MockResponse dispatch(RecordedRequest request) {
            String key = request.getHeader("Idempotency-Key");
            seenKeys.add(key);

            if (key == null || key.isBlank()) {
                return slowSuccess(newPaymentKey());
            }
            String existing = paymentKeyByIdempotencyKey.get(key);
            if (existing != null) {
                return fastSuccess(existing);
            }
            String paymentKey = newPaymentKey();
            paymentKeyByIdempotencyKey.put(key, paymentKey);
            return slowSuccess(paymentKey);
        }

        int createdPayments() {
            return createdPayments.get();
        }

        List<String> seenKeys() {
            return seenKeys;
        }

        private String newPaymentKey() {
            return "pk_" + createdPayments.incrementAndGet();
        }

        private MockResponse slowSuccess(String paymentKey) {
            return success(paymentKey).setHeadersDelay(2, TimeUnit.SECONDS);
        }

        private MockResponse fastSuccess(String paymentKey) {
            return success(paymentKey);
        }

        private MockResponse success(String paymentKey) {
            return new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""
                            {"paymentKey": "%s", "orderId": "order-1", "status": "DONE", "totalAmount": 10000}
                            """.formatted(paymentKey));
        }
    }
}
