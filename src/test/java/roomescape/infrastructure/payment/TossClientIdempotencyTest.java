package roomescape.infrastructure.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentStatus;
import roomescape.exception.TossPaymentException;

/**
 * 타임아웃의 또 다른 위험을 다룬다. read timeout 은 "요청은 도달했는데 응답만 못 받은" 상태를 만들 수 있어, 끊긴 뒤 재시도하면 같은 결제가 두 번 일어날 수 있다. Idempotency-Key
 * 로 그 중복을 막는 것을 확인한다.
 *
 * <p>선행: 이 테스트는 read timeout 이 동작해야 시나리오가 성립한다. 타임아웃 학습 테스트를 먼저 통과시킨 뒤 풀자.
 */
@SpringBootTest
class TossClientIdempotencyTest {

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
        registry.add("toss.secret-key", () -> "test_gsk_dummy");
        registry.add("toss.connect-timeout-ms", () -> "500");
        registry.add("toss.read-timeout-ms", () -> "500");
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("test_pk_1", "order-1", 10000L);
    }

    @Test
    void read_timeout으로_끊겨_재시도해도_같은_멱등키면_중복_결제가_생기지_않는다() {
        var stub = new IdempotentGatewayStub();
        mockWebServer.setDispatcher(stub);
        var confirmation = confirmation();

        // 1차: 서버는 결제를 처리하지만 응답이 느려 read timeout(500ms)으로 끊긴다.
        //      클라이언트는 실패로 알지만 서버에는 이미 결제가 만들어졌다 — 응답만 유실된 것.
        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation))
                .isInstanceOf(TossPaymentException.ReadTimeout.class);

        // 2차(재시도): 같은 Idempotency-Key 라 서버가 중복을 인지해 재처리 없이 첫 결과를 즉시 돌려준다.
        var result = tossPaymentGateway.confirm(confirmation);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        // 두 번 호출됐지만 서버가 만든 결제는 1건뿐 — 이중 청구가 아니다.
        assertThat(stub.createdPayments()).isEqualTo(1);
        // 중복으로 묶일 수 있었던 건, 두 요청이 같은(그리고 비어 있지 않은) 키를 실어 보냈기 때문이다.
        assertThat(stub.seenKeys()).hasSize(2);
        assertThat(stub.seenKeys().get(0)).isNotBlank().isEqualTo(stub.seenKeys().get(1));
    }

    /**
     * 멱등키로 중복 요청을 걸러내는 결제 게이트웨이 스텁. 처음 보는 키는 결제를 만들되 응답을 느리게 줘 (read timeout 유발), 같은 키 재요청은 첫 결과를 즉시 돌려준다. 키가 없으면 매 요청을
     * 새 결제로 처리한다.
     */
    private static class IdempotentGatewayStub extends Dispatcher {

        private final Map<String, String> paymentKeyByIdempotencyKey = new ConcurrentHashMap<>();
        private final AtomicInteger createdPayments = new AtomicInteger();
        private final List<String> seenKeys = new CopyOnWriteArrayList<>();

        @Override
        public MockResponse dispatch(RecordedRequest request) {
            var key = request.getHeader("Idempotency-Key");
            seenKeys.add(key);

            // 키가 없으면 매 요청을 새 결제로 처리한다 — 재시도가 곧 중복 결제가 된다.
            if (key == null || key.isBlank()) {
                return slowSuccess(newPaymentKey());
            }
            // 이미 처리한 키면 재처리 없이 첫 결과를 즉시 돌려준다(멱등).
            var existing = paymentKeyByIdempotencyKey.get(key);
            if (existing != null) {
                return fastSuccess(existing);
            }
            // 처음 보는 키: 결제를 만들고 결과를 기록한다. 단 응답이 느려 클라이언트는 이번엔 받지 못한다.
            var paymentKey = newPaymentKey();
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
