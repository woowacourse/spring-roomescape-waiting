package roomescape.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.exception.PaymentErrorCode;

class TossPaymentGatewayTimeoutTest {

    private static final Duration READ_TIMEOUT = Duration.ofMillis(500);

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() {
        try {
            mockWebServer.shutdown();
        } catch (Exception alreadyClosed) {
            // 연결 실패 테스트에서 이미 종료된 경우 무시한다.
        }
    }

    private TossPaymentGateway gatewayFor(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(500));
        factory.setReadTimeout(READ_TIMEOUT);
        RestClient restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        return new TossPaymentGateway(restClient, new ObjectMapper());
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("ORDER-12345678", "test_payment_key", 1000L, "idem-key-1");
    }

    @Test
    void 응답이_read_timeout보다_느리면_일찍_포기하고_결과_불명확으로_표면화한다() {
        // given : read timeout(500ms)보다 한참 느린 응답
        mockWebServer.enqueue(new MockResponse()
                .setBodyDelay(3, TimeUnit.SECONDS)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"paymentKey\":\"pk\",\"orderId\":\"ORDER-12345678\",\"status\":\"DONE\",\"totalAmount\":1000}"));
        TossPaymentGateway gateway = gatewayFor(mockWebServer.url("/").toString());

        // when
        long start = System.currentTimeMillis();
        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() -> gateway.confirm(confirmation()));
        long elapsed = System.currentTimeMillis() - start;

        // then : 느린 응답을 끝까지 기다리지 않고(3초 X) read timeout 근처에서 포기한다
        assertThat(thrown).isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_RESULT_UNKNOWN);
        assertThat(elapsed).isLessThan(2000L);
    }

    @Test
    void 연결할_수_없으면_연결_실패로_표면화한다() throws IOException {
        // given : 서버를 닫아 연결이 거부되도록 한다
        String deadUrl = mockWebServer.url("/").toString();
        mockWebServer.shutdown();
        TossPaymentGateway gateway = gatewayFor(deadUrl);

        // when & then
        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_CONNECTION_FAILED);
    }
}
