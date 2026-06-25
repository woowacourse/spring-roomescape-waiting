package roomescape.payment.infrastructure.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.exception.PaymentGatewayConnectionTimeoutException;
import roomescape.payment.domain.exception.PaymentGatewayResponseTimeoutException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TossPaymentGatewayTimeoutTest {

    // 응답 없는(SYN 무응답) IP → connect 가 매달려 connect timeout 을 유발한다.
    // 일부 환경(사내망/CI)에선 즉시 거부 응답이 와서 시나리오가 깨질 수 있으니, 본인 환경에서 침묵하는 IP인지 먼저 확인하라.
    private static final String BLACKHOLE_URL = "http://10.255.255.1:81";

    private static final String SUCCESS_BODY = """
            {"paymentKey": "test_pk_1", "orderId": "order-1", "status": "DONE", "totalAmount": 10000}
            """;

    private static final MockWebServer MOCK_WEB_SERVER = startServer();

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.toss.base-url", () -> MOCK_WEB_SERVER.url("/").toString());
        registry.add("payment.toss.secret-key", () -> "test_sk_dummy");
        registry.add("payment.toss.connect-timeout-ms", () -> "500");
        registry.add("payment.toss.read-timeout-ms", () -> "500");
    }

    @AfterAll
    static void tearDown() throws IOException {
        MOCK_WEB_SERVER.shutdown();
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("test_pk_1", "order-1", 10000L);
    }

    @Test
    void 읽기타임아웃이면_readTimeout만큼만_기다렸다가_도메인_예외로_실패한다() {
        MOCK_WEB_SERVER.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY)
                .setHeadersDelay(2, TimeUnit.SECONDS));

        long start = System.nanoTime();
        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation()))
                .isInstanceOf(PaymentGatewayResponseTimeoutException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // 서버는 2초를 끌지만 read timeout(500ms) 이 먼저 끊는다.
        assertThat(elapsedMs).isLessThan(1500);
    }

    @Test
    void 느린_호출이_섞여도_타임아웃이_있으면_성공_TPS가_유지된다() {
        // 느린 응답(2초)과 정상 응답이 번갈아 온다 — 느린 의존성에 일부 호출만 물리는 상황.
        for (int i = 0; i < 3; i++) {
            MOCK_WEB_SERVER.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(SUCCESS_BODY)
                    .setHeadersDelay(2, TimeUnit.SECONDS));
            MOCK_WEB_SERVER.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(SUCCESS_BODY));
        }

        int succeeded = 0;
        long start = System.nanoTime();
        for (int i = 0; i < 6; i++) {
            try {
                tossPaymentGateway.confirm(confirmation());
                succeeded++;
            } catch (PaymentGatewayResponseTimeoutException e) {
                // 타임아웃으로 일찍 포기한 호출 — 성공 TPS 에 세지 않는다.
            }
        }
        double elapsedSeconds = (System.nanoTime() - start) / 1_000_000_000.0;
        double tps = succeeded / elapsedSeconds;

        // read timeout(500ms) 이 있으면 느린 3건을 일찍 포기한 덕에 정상 3건이 제때 처리된다 → ~1.8.
        // 없으면 느린 호출이 스레드를 2초씩 붙잡아, 6건 전부 성공하고도 1.0 을 넘지 못한다.
        assertThat(tps).isGreaterThan(1.1);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    void 라우팅불가_IP면_connectTimeout만큼_기다렸다가_SocketTimeout으로_실패한다() {
        // TossClientConfig.tossRestClient 가 채워둔 connect timeout 을 그대로 검증한다.
        // 설정 전(initial)엔 타임아웃이 없어 블랙홀 연결이 매달리므로 @Timeout(3초) 이 끊어 실패시킨다.
        TossPaymentGateway gateway = new TossPaymentGateway(
                new TossClientConfig().tossRestClient(BLACKHOLE_URL, "test_sk_dummy", 500, 500),
                new ObjectMapper());

        long start = System.nanoTime();
        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(PaymentGatewayConnectionTimeoutException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // connect timeout(500ms) 만큼 기다렸다가 끊긴다.
        assertThat(elapsedMs).isBetween(300L, 2500L);
    }

    private static MockWebServer startServer() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return server;
    }
}
