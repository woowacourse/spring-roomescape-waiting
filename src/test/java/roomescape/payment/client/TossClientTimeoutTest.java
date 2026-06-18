package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.exception.PaymentConnectionFailedException;
import roomescape.payment.exception.PaymentTimedOutException;

@SpringBootTest
class TossClientTimeoutTest {

    // 응답 없는(SYN 무응답) IP → connect 가 매달려 connect timeout 을 유발한다.
    private static final String BLACKHOLE_URL = "http://10.255.255.1:81";

    static MockWebServer mockWebServer;

    static {
        mockWebServer = new MockWebServer();
        try {
            mockWebServer.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final String SUCCESS_BODY = """
            {"paymentKey": "test_pk_1", "orderId": "order-1", "status": "DONE", "totalAmount": 10000}
            """;

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
    void 읽기타임아웃이면_readTimeout만큼만_기다렸다가_RestClient예외로_실패한다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY)
                .setHeadersDelay(2, TimeUnit.SECONDS));

        var start = System.nanoTime();
        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation()))
                .isInstanceOf(PaymentTimedOutException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        var elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // 서버는 2초를 끌지만 read timeout(500ms)이 먼저 끊는다.
        assertThat(elapsedMs).isLessThan(1500);
    }

    @Test
    void 느린_호출이_섞여도_타임아웃이_있으면_성공_TPS가_유지된다() {
        for (var i = 0; i < 3; i++) {
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

        var succeeded = 0;
        var start = System.nanoTime();
        for (var i = 0; i < 6; i++) {
            try {
                tossPaymentGateway.confirm(confirmation());
                succeeded++;
            } catch (PaymentTimedOutException e) {
                // 타임아웃으로 일찍 포기한 호출
            }
        }
        var elapsedSeconds = (System.nanoTime() - start) / 1_000_000_000.0;
        var tps = succeeded / elapsedSeconds;

        assertThat(tps).isGreaterThan(1.1);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    void 라우팅불가_IP면_connectTimeout만큼_기다렸다가_SocketTimeout으로_실패한다() {
        var gateway = new TossPaymentGateway(
                new TossClientConfig().tossRestClient(BLACKHOLE_URL, "test_gsk_dummy", 500, 500));

        var start = System.nanoTime();
        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(PaymentConnectionFailedException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        var elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(elapsedMs).isBetween(300L, 2500L);
    }
}
