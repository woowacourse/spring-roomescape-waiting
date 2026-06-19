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
import roomescape.global.exception.RetryablePaymentGatewayException;
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
        // 서버는 2초 뒤 응답하지만 read timeout(500ms)이 두 번의 승인 시도를 모두 먼저 끊어야 한다.
        enqueueDelayedSuccess();
        enqueueDelayedSuccess();

        long start = System.nanoTime();

        assertThatThrownBy(() -> tossPaymentGateway.confirm(paymentConfirmation()))
                .isInstanceOf(RetryablePaymentGatewayException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // timeout이 없다면 첫 응답만으로도 2초 이상 기다리므로, 두 번 시도해도 2초 미만이면 read timeout이 적용된 것이다.
        assertThat(elapsedMs).isLessThan(2_000L);
    }

    @DisplayName("느린 호출이 섞여도 timeout이 있으면 성공 TPS가 유지됩니다.")
    @Test
    void confirm_maintains_success_tps_when_slow_calls_are_mixed_with_fast_calls() {
        // 느린 호출은 자동 재시도까지 두 번 timeout되고, 빠른 호출은 즉시 성공하는 상황을 만든다.
        for (int i = 0; i < 2; i++) {
            enqueueDelayedSuccess();
            enqueueDelayedSuccess();
            enqueueSuccess();
            enqueueSuccess();
        }

        int succeeded = 0;
        long start = System.nanoTime();
        for (int i = 0; i < 6; i++) {
            try {
                tossPaymentGateway.confirm(paymentConfirmation());
                succeeded++;
            } catch (RetryablePaymentGatewayException e) {
                // timeout으로 일찍 포기한 호출은 성공 TPS에 세지 않는다.
            }
        }
        double elapsedSeconds = (System.nanoTime() - start) / 1_000_000_000.0;

        assertThat(succeeded).isEqualTo(4);
        // timeout이 없다면 느린 응답 4개를 순서대로 기다려 8초 이상 걸리므로, 느린 호출을 일찍 포기해야 한다.
        assertThat(elapsedSeconds).isLessThan(6.0);
    }

    @DisplayName("라우팅 불가 IP면 connect timeout 시간만큼 기다린 뒤 실패합니다.")
    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    void confirm_fails_after_connect_timeout_when_toss_host_does_not_respond() {
        // Spring context 대신 timeout 값이 들어간 RestClient를 직접 만들어 connect timeout만 검증한다.
        TossPaymentGateway gateway = new TossPaymentGateway(
                new TossClientConfig().tossRestClient(BLACKHOLE_URL, "test_secret_key", 500, 500, 2, 100, 100.0),
                new ObjectMapper()
        );

        long start = System.nanoTime();

        assertThatThrownBy(() -> gateway.confirm(paymentConfirmation()))
                .isInstanceOf(RetryablePaymentGatewayException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // connect timeout(500ms)이 두 번의 승인 시도에 각각 적용되어야 하고, 네트워크 편차를 고려해 상한을 둔다.
        assertThat(elapsedMs).isBetween(700L, 4_500L);
    }

    private void enqueueDelayedSuccess() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY)
                .setHeadersDelay(2, TimeUnit.SECONDS));
    }

    private void enqueueSuccess() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY));
    }

    private PaymentConfirmation paymentConfirmation() {
        return new PaymentConfirmation("payment-key", "order-id", 1_000L);
    }
}
