package roomescape.reservation.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import roomescape.reservation.application.port.out.payment.PaymentConfirmation;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class TossClientTimeoutTest {

    // 응답 없는(SYN 무응답) IP로 connect timeout을 검증한다.
    private static final String BLACKHOLE_URL = "http://10.255.255.1:81";

    // 실제 토스 서버 대신 테스트 안에서 띄우는 HTTP 서버다.
    private static final MockWebServer mockWebServer;
    private static final String SUCCESS_BODY = """
            {
              "paymentKey": "payment-key",
              "orderId": "order-id",
              "status": "DONE",
              "totalAmount": 1000
            }
            """;

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
        // 테스트에서는 Toss base URL을 MockWebServer 주소로 바꿔치기한다.
        registry.add("toss.base-url", () -> mockWebServer.url("/").toString());
        registry.add("toss.secret-key", () -> "test_secret_key");
        registry.add("toss.connect-timeout-ms", () -> "500");
        registry.add("toss.read-timeout-ms", () -> "500");
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DisplayName("토스 응답이 지연되면 read timeout 시간만큼 기다린 뒤 실패합니다.")
    @Test
    void confirm_fails_after_read_timeout_when_toss_response_is_delayed() {
        // 서버는 2초 뒤 응답하지만 read timeout(500ms)이 먼저 끊어야 한다.
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY)
                .setHeadersDelay(2, TimeUnit.SECONDS));

        long start = System.nanoTime();

        assertThatThrownBy(() -> tossPaymentGateway.confirm(paymentConfirmation()))
                .isInstanceOf(RestClientException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // timeout이 없다면 2초 이상 기다리므로, 1.5초 미만이면 read timeout이 적용된 것이다.
        assertThat(elapsedMs).isLessThan(1_500L);
    }

    @DisplayName("느린 호출이 섞여도 timeout이 있으면 성공 TPS가 유지됩니다.")
    @Test
    void confirm_maintains_success_tps_when_slow_calls_are_mixed_with_fast_calls() {
        // 느린 응답(2초)과 정상 응답이 번갈아 와서, 일부 호출만 느린 의존성에 물리는 상황을 만든다.
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
                tossPaymentGateway.confirm(paymentConfirmation());
                succeeded++;
            } catch (RestClientException e) {
                // timeout으로 일찍 포기한 호출은 성공 TPS에 세지 않는다.
            }
        }
        double elapsedSeconds = (System.nanoTime() - start) / 1_000_000_000.0;
        double tps = succeeded / elapsedSeconds;

        // read timeout(500ms)이 있으면 느린 3건을 일찍 포기해서 정상 3건이 제때 처리된다.
        assertThat(tps).isGreaterThan(1.1);
    }

    @DisplayName("라우팅 불가 IP면 connect timeout 시간만큼 기다린 뒤 실패합니다.")
    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    void confirm_fails_after_connect_timeout_when_toss_host_does_not_respond() {
        // Spring context 대신 timeout 값이 들어간 RestClient를 직접 만들어 connect timeout만 검증한다.
        TossPaymentGateway gateway = new TossPaymentGateway(
                new TossClientConfig().tossRestClient(BLACKHOLE_URL, "test_secret_key", 500, 500),
                new ObjectMapper()
        );

        long start = System.nanoTime();

        assertThatThrownBy(() -> gateway.confirm(paymentConfirmation()))
                .isInstanceOf(ResourceAccessException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // connect timeout(500ms) 근처에서 끊겨야 하고, 네트워크 편차를 고려해 상한을 넉넉히 둔다.
        assertThat(elapsedMs).isBetween(300L, 2_500L);
    }

    private PaymentConfirmation paymentConfirmation() {
        return new PaymentConfirmation("payment-key", "order-id", 1_000L);
    }
}
