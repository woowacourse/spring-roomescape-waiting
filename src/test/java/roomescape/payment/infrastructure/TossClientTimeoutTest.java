package roomescape.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.payment.domain.PaymentConfirmation;

class TossClientTimeoutTest {

    // 응답 없는(SYN 무응답) IP → connect 가 매달려 connect timeout 을 유발한다.
    private static final String BLACKHOLE_URL = "http://10.255.255.1:81";

    private static final String SUCCESS_BODY = """
            {"paymentKey": "test_pk_1", "orderId": "order-1", "status": "DONE", "totalAmount": 10000}
            """;

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

    private TossPaymentGateway gatewayWithTimeouts(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {
        TossProperties properties = new TossProperties(baseUrl, "", "test_gsk_dummy", connectTimeoutMs, readTimeoutMs, 3);
        RestClient restClient = new TossClientConfig().tossRestClient(properties, 1000, 1000);
        return new TossPaymentGateway(restClient, new ObjectMapper());
    }

    @Test
    void 읽기타임아웃이면_readTimeout만큼만_기다렸다가_RestClient예외로_실패한다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY)
                .setHeadersDelay(2, TimeUnit.SECONDS));

        TossPaymentGateway gateway = gatewayWithTimeouts(mockWebServer.url("/").toString(), 500, 500);

        long start = System.nanoTime();
        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(RestClientException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // 서버는 2초를 끌지만 read timeout(500ms)이 먼저 끊는다.
        assertThat(elapsedMs).isLessThan(1500);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    void 라우팅불가_IP면_connectTimeout만큼_기다렸다가_SocketTimeout으로_실패한다() {
        TossPaymentGateway gateway = gatewayWithTimeouts(BLACKHOLE_URL, 500, 500);

        long start = System.nanoTime();
        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(ResourceAccessException.class)
                .hasCauseInstanceOf(SocketTimeoutException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // connect timeout(500ms)만큼 기다렸다가 끊긴다.
        assertThat(elapsedMs).isBetween(300L, 2500L);
    }
}
