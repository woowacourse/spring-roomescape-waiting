package roomescape.payment.gateway.toss;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.payment.PaymentResultUnknownException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentStatus;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
class TossPaymentGatewayTest {

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
        registry.add("toss.base-url", () -> mockWebServer.url("/").toString());
        registry.add("toss.secret-key", () -> "test_gsk_dummy");
        // 느린 응답을 일찍 포기하는지 확인하기 위해 read timeout을 짧게 둔다
        registry.add("toss.connect-timeout-ms", () -> 1000);
        registry.add("toss.read-timeout-ms", () -> 1000);
        registry.add("outbound-rate-limit.capacity", () -> 1000);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private void enqueue(int statusCode, String body) {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(statusCode)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }

    @Test
    void confirm이_성공하면_status가_DONE인_결과를_반환한다() {
        enqueue(200, """
                {
                  "paymentKey": "test_pk_1",
                  "orderId": "order-1",
                  "status": "DONE",
                  "totalAmount": 50000
                }
                """);

        var result = tossPaymentGateway.confirm(new PaymentConfirmation("test_pk_1", "order-1", 50000L, "idem-1"));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(50000L);
    }

    @Test
    void read_timeout이면_PaymentResultUnknownException으로_표면화된다() {
        // 응답 바디를 read timeout보다 늦게 흘려보내 읽기 단계 타임아웃을 유발한다
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"paymentKey\":\"pk\",\"orderId\":\"order-1\",\"status\":\"DONE\",\"totalAmount\":50000}")
                .setBodyDelay(3, TimeUnit.SECONDS));

        assertThatThrownBy(() -> tossPaymentGateway.confirm(new PaymentConfirmation("test_pk_1", "order-1", 50000L, "idem-1")))
                .isInstanceOf(PaymentResultUnknownException.class);
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    void 에러코드별로_매핑된_예외가_던져진다(int httpStatus, String code, Class<? extends Throwable> expected) {
        enqueue(httpStatus, "{\"code\": \"" + code + "\", \"message\": \"에러 메시지\"}");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(new PaymentConfirmation("test_pk_1", "order-1", 50000L, "idem-1")))
                .isInstanceOf(expected);
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                arguments(400, "ALREADY_PROCESSED_PAYMENT",              TossPaymentException.AlreadyProcessed.class),
                arguments(400, "DUPLICATED_ORDER_ID",                    TossPaymentException.DuplicatedOrder.class),
                arguments(400, "NOT_FOUND_PAYMENT_SESSION",              TossPaymentException.SessionExpired.class),
                arguments(400, "INVALID_REQUEST",                        TossPaymentException.InvalidRequest.class),
                arguments(401, "UNAUTHORIZED_KEY",                       TossPaymentException.GatewayConfig.class),
                arguments(401, "INVALID_API_KEY",                        TossPaymentException.GatewayConfig.class),
                arguments(403, "REJECT_CARD_PAYMENT",                    TossPaymentException.CardRejected.class),
                arguments(404, "NOT_FOUND_PAYMENT",                      TossPaymentException.PaymentNotFound.class),
                arguments(500, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", TossPaymentException.Retryable.class),
                arguments(400, "SOME_UNDEFINED_CODE",                    TossPaymentException.class)
        );
    }
}
