package roomescape.payment.client;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentResultUnknownException;
import roomescape.payment.PaymentStatus;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * read timeout 으로 끊긴 뒤 같은 멱등키로 재시도해도 이중 승인되지 않는지 검증한다.
 * 1차는 응답 지연으로 끊기지만(서버엔 결제 생성), 2차는 같은 Idempotency-Key 라
 * 서버가 첫 결과를 즉시 돌려준다 → 결제는 1건만 생성된다.
 */
@SpringBootTest
class TossPaymentGatewayIdempotencyTest {

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
        // 별도 컨텍스트가 공유 DB(jdbc:h2:mem:database)와 충돌하지 않도록 고유 인메모리 DB 사용
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:idempotency-test");
        registry.add("toss.base-url", () -> mockWebServer.url("/").toString());
        registry.add("toss.secret-key", () -> "test_gsk_dummy");
        registry.add("toss.connect-timeout-ms", () -> "500");
        registry.add("toss.read-timeout-ms", () -> "500");
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("test_pk_1", "order-1", 10000L, "fixed-idem-key");
    }

    @Test
    void read_timeout으로_끊겨_재시도해도_같은_멱등키면_중복_결제가_생기지_않는다() {
        IdempotentGatewayStub stub = new IdempotentGatewayStub();
        mockWebServer.setDispatcher(stub);
        PaymentConfirmation confirmation = confirmation();

        // 1차: 응답이 느려 read timeout 으로 끊긴다. 서버엔 이미 결제가 생성된 상태.
        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation))
                .isInstanceOf(PaymentResultUnknownException.class);

        // 2차(재시도): 같은 멱등키라 서버가 재처리 없이 첫 결과를 즉시 반환한다.
        PaymentResult result = tossPaymentGateway.confirm(confirmation);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(stub.createdPayments()).isEqualTo(1);
        assertThat(stub.seenKeys()).hasSize(2);
        assertThat(stub.seenKeys().get(0)).isNotBlank().isEqualTo(stub.seenKeys().get(1));
    }

    private static class IdempotentGatewayStub extends Dispatcher {

        private final Map<String, String> paymentKeyByKey = new ConcurrentHashMap<>();
        private final AtomicInteger createdPayments = new AtomicInteger();
        private final List<String> seenKeys = new CopyOnWriteArrayList<>();

        @Override
        public MockResponse dispatch(RecordedRequest request) {
            String key = request.getHeader("Idempotency-Key");
            seenKeys.add(key);

            if (key == null || key.isBlank()) {
                return slowSuccess(newPaymentKey());
            }
            String existing = paymentKeyByKey.get(key);
            if (existing != null) {
                return fastSuccess(existing);
            }
            String paymentKey = newPaymentKey();
            paymentKeyByKey.put(key, paymentKey);
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
