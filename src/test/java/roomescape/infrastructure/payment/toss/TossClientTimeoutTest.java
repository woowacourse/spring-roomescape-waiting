package roomescape.infrastructure.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpTimeoutException;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import roomescape.infrastructure.payment.PaymentConfirmation;

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
                .isInstanceOf(RestClientException.class)
                .hasRootCauseInstanceOf(HttpTimeoutException.class);
        var elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // 서버는 2초를 끌지만 read timeout(500ms)이 먼저 끊는다.
        assertThat(elapsedMs).isLessThan(1500);
    }

    @Test
    void 느린_호출이_섞여도_타임아웃이_있으면_성공_TPS가_유지된다() {
        // 느린 응답(2초)과 정상 응답이 번갈아 온다 — 느린 의존성에 일부 호출만 물리는 상황.
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
            } catch (RestClientException e) {
                // 타임아웃으로 일찍 포기한 호출 — 성공 TPS 에 세지 않는다.
            }
        }
        var elapsedSeconds = (System.nanoTime() - start) / 1_000_000_000.0;
        var tps = succeeded / elapsedSeconds;

        // 성공 TPS = 성공 건수 ÷ 경과 초.
        // read timeout(500ms)이 있으면 느린 3건을 일찍 포기한 덕에 정상 3건이 제때 처리된다 → ~1.8.
        // 없으면 느린 호출이 스레드를 2초씩 붙잡아, 6건 전부 성공하고도 1.0 을 넘지 못한다.
        assertThat(tps).isGreaterThan(1.1);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    void 라우팅불가_IP면_connectTimeout만큼_기다렸다가_SocketTimeout으로_실패한다() {
        // 학생이 채운 tossRestClient 의 connect timeout 을 그대로 검증한다.
        // 설정 전(initial)엔 타임아웃이 없어 블랙홀 연결이 매달리므로 @Timeout(3초)이 끊어 실패시킨다.
        var gateway = new TossPaymentGateway(
                new TossClientConfig().tossRestClient(BLACKHOLE_URL, "test_gsk_dummy", 500, 500),
                new ObjectMapper()
        );

        var start = System.nanoTime();
        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(ResourceAccessException.class)
                .hasCauseInstanceOf(HttpTimeoutException.class);
        var elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // connect timeout(500ms)만큼 기다렸다가 끊긴다.
        assertThat(elapsedMs).isBetween(300L, 2500L);
    }

}
