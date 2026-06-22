package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
import roomescape.client.TossPaymentGateway;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentStatus;

/**
 * 토스가 429 를 줘서 RetryAfterInterceptor 가 재시도할 때, 2단계에서 도입한 주문당 고정 멱등키
 * (Idempotency-Key=orderId)가 재시도 요청에도 그대로 실려 나가는지 검증한다.
 *
 * <p>read timeout 재시도(TossClientIdempotencyTest)와는 다른 경로다. 그쪽은 confirm() 을 다시 호출하는
 * 애플리케이션 레벨 재시도이고, 이쪽은 한 번의 confirm() 안에서 인터셉터가 같은 요청을 재실행하는
 * 인터셉터 레벨 재시도다 — 두 경로 모두 같은 멱등키를 유지해야 중복 승인을 막을 수 있다.
 */
@SpringBootTest
class RetryAfterIdempotencyTest {

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
        registry.add("toss.max-attempts", () -> "3");
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void _429로_재시도해도_같은_멱등키가_재전송된다() {
        var stub = new RetryAfterOnceStub();
        mockWebServer.setDispatcher(stub);
        var confirmation = new PaymentConfirmation("test_pk_1", "order-1", 10000L);

        var result = tossPaymentGateway.confirm(confirmation);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        // 429 → 재시도 → 200, 두 번 호출됐다.
        assertThat(stub.seenKeys()).hasSize(2);
        // 두 요청 모두 비어 있지 않은 같은 키(=orderId)를 실어 보냈다 — 재시도가 멱등키를 잃지 않았다.
        assertThat(stub.seenKeys().get(0)).isEqualTo("order-1");
        assertThat(stub.seenKeys().get(1)).isEqualTo("order-1");
    }

    /**
     * 첫 요청엔 429 + Retry-After 로 백오프를 유도하고, 그 다음 요청엔 200 을 준다.
     * 매 요청의 Idempotency-Key 헤더를 기록해 재시도에도 키가 유지되는지 검증할 수 있게 한다.
     */
    private static class RetryAfterOnceStub extends Dispatcher {

        private final List<String> seenKeys = new CopyOnWriteArrayList<>();

        @Override
        public MockResponse dispatch(RecordedRequest request) {
            seenKeys.add(request.getHeader("Idempotency-Key"));
            if (seenKeys.size() == 1) {
                return new MockResponse()
                        .setResponseCode(429)
                        .setHeader("Retry-After", "1");
            }
            return new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""
                            {"paymentKey": "pk_1", "orderId": "order-1", "status": "DONE", "totalAmount": 10000}
                            """);
        }

        List<String> seenKeys() {
            return seenKeys;
        }
    }
}
