package roomescape.payment.toss;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TossPaymentGatewayIdempotencyTest {

    private static final Duration READ_TIMEOUT = Duration.ofMillis(500);
    private static final String DONE_BODY =
            "{\"paymentKey\":\"pk_1\",\"orderId\":\"order-1\",\"status\":\"DONE\"}";

    private MockWebServer mockWebServer;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(READ_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        RestClient restClient = RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestFactory(requestFactory)
                .build();
        gateway = new TossPaymentGateway(restClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 첫_시도가_타임아웃돼_재시도해도_같은_멱등키면_서버가_결제를_한_건만_만든다() {
        IdempotentGatewayStub stub = new IdempotentGatewayStub();
        mockWebServer.setDispatcher(stub);

        PaymentResult result = gateway.confirm(new PaymentConfirmation("pk_1", "order-1", 50_000L));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        // 두 번 호출됐지만 서버가 만든 결제는 1건뿐 — 이중 청구가 아니다.
        assertThat(stub.createdPayments()).isEqualTo(1);
        // 중복으로 묶일 수 있었던 건, 두 요청이 같은(그리고 비어 있지 않은) 키를 실어 보냈기 때문이다.
        assertThat(stub.seenKeys()).hasSize(2);
        assertThat(stub.seenKeys().get(0)).isNotBlank().isEqualTo(stub.seenKeys().get(1));
    }

    private static class IdempotentGatewayStub extends Dispatcher {

        private final List<String> seenKeys = new ArrayList<>();
        private int createdPayments = 0;

        @Override
        public MockResponse dispatch(RecordedRequest request) {
            String idempotencyKey = request.getHeader("Idempotency-Key");
            boolean firstSight = !seenKeys.contains(idempotencyKey);
            seenKeys.add(idempotencyKey);

            MockResponse response = new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(DONE_BODY);
            if (firstSight) {
                createdPayments++;
                // 첫 요청: 서버는 결제를 처리하지만 응답이 느려 client가 read timeout으로 끊긴다.
                return response.setBodyDelay(2, TimeUnit.SECONDS);
            }
            // 같은 키 재시도: 재처리 없이 첫 결과를 즉시 돌려준다.
            return response;
        }

        private List<String> seenKeys() {
            return seenKeys;
        }

        private int createdPayments() {
            return createdPayments;
        }
    }
}
