package roomescape.client.toss;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.client.PaymentConfirmation;
import roomescape.client.PaymentConfirmationUnknownException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TossClientTimeoutTest {

    private static final String BLACKHOLE_URL = "http://10.255.255.1:81";
    private static final MockWebServer mockWebServer = new MockWebServer();
    private static final String SUCCESS_BODY = """
            {"paymentKey": "test_pk_1", "orderId": "order-1", "status": "DONE", "totalAmount": 10000}
            """;

    static {
        try {
            mockWebServer.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    private TossPaymentGateWay tossPaymentGateWay;

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

    @Test
    void 읽기_타임아웃이면_readTimeout만큼만_기다렸다가_확인_필요_예외로_변환한다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY)
                .setHeadersDelay(2, TimeUnit.SECONDS));

        long start = System.nanoTime();
        assertThatThrownBy(() -> tossPaymentGateWay.confirm(confirmation()))
                .isInstanceOf(PaymentConfirmationUnknownException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(elapsedMs).isLessThan(1_500);
    }

    @Test
    void 느린_호출이_섞여도_타임아웃이_있으면_정상_응답을_제때_처리한다() {
        for (int i = 0; i < 3; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(SUCCESS_BODY)
                    .setHeadersDelay(2, TimeUnit.SECONDS));
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(SUCCESS_BODY));
        }

        int succeeded = 0;
        long start = System.nanoTime();
        for (int i = 0; i < 6; i++) {
            try {
                tossPaymentGateWay.confirm(confirmation());
                succeeded++;
            } catch (PaymentConfirmationUnknownException e) {
                // 타임아웃으로 일찍 포기한 호출은 성공 처리량에 세지 않는다.
            }
        }
        double elapsedSeconds = (System.nanoTime() - start) / 1_000_000_000.0;
        double tps = succeeded / elapsedSeconds;

        assertThat(succeeded).isEqualTo(3);
        assertThat(tps).isGreaterThan(1.1);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void 라우팅_불가_IP면_connectTimeout만큼_기다렸다가_확인_필요_예외로_변환한다() {
        TossPaymentGateWay gateway = new TossPaymentGateWay(
                new TossClientConfig().tossRestClient(BLACKHOLE_URL, "test_gsk_dummy", 500, 500),
                new com.fasterxml.jackson.databind.ObjectMapper()
        );

        long start = System.nanoTime();
        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(PaymentConfirmationUnknownException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(elapsedMs).isBetween(300L, 2_500L);
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("test_pk_1", "order-1", 10_000L, "idempotency-key-1");
    }
}
