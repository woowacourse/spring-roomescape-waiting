package roomescape.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentResultUnknownException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 타임아웃이 도메인 예외로 올바르게 표면화되는지 검증한다.
 * read timeout(응답 지연)은 "확인 필요"(PaymentResultUnknownException),
 * 연결 단계 실패(라우팅 불가 IP)는 "재시도 가능"(PaymentConnectionException)으로 구분되어야 한다.
 */
@SpringBootTest
class TossPaymentGatewayTimeoutTest {

    // 응답 없는(SYN 무응답) IP → connect 가 매달려 connect timeout 을 유발한다.
    private static final String BLACKHOLE_URL = "http://10.255.255.1:81";

    private static final String SUCCESS_BODY = """
            {"paymentKey": "test_pk_1", "orderId": "order-1", "status": "DONE", "totalAmount": 10000}
            """;

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
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:timeout-test");
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
        return new PaymentConfirmation("test_pk_1", "order-1", 10000L, "idem-key-1");
    }

    @Test
    void 읽기타임아웃이면_readTimeout만큼만_기다렸다가_확인필요_예외로_실패한다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY)
                .setHeadersDelay(2, TimeUnit.SECONDS));

        long start = System.nanoTime();
        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation()))
                .isInstanceOf(PaymentResultUnknownException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // 서버는 2초를 끌지만 read timeout(500ms)이 먼저 끊는다.
        assertThat(elapsedMs).isLessThan(1500);
    }

    @Test
    @Timeout(3)
    void 라우팅불가_IP면_connectTimeout만큼_기다렸다가_연결실패_예외로_실패한다() {
        TossPaymentGateway gateway = new TossPaymentGateway(
                new TossClientConfig().tossRestClient(BLACKHOLE_URL, "test_gsk_dummy", 500, 500, 1, 100, 100.0),
                new ObjectMapper());

        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(PaymentConnectionException.class);
    }
}
