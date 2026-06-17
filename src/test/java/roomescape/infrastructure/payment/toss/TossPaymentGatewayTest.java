package roomescape.infrastructure.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.order.PaymentStatus;
import roomescape.infrastructure.payment.PaymentConfirmation;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    @DisplayName("confirm이 성공하면 status가 DONE인 결과를 반환한다.")
    void confirmPayment_Success() {
        enqueue(200, """
                {
                  "paymentKey": "test_pk_1",
                  "orderId": "order-1",
                  "orderName": "방탈출 예약",
                  "status": "DONE",
                  "totalAmount": 10000,
                  "balanceAmount": 10000,
                  "method": "카드",
                  "approvedAt": "2026-06-08T12:00:00+09:00",
                  "requestedAt": "2026-06-08T11:59:30+09:00"
                }
                """);

        var result = tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 10000L));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("이미 처리된 결제면 AlreadyProcessed가 던져진다.")
    void confirmPayment_WhenPaymentIsAlreadyProcessed_ThrowAlreadyProcessed() {
        enqueue(400, """
                {"code": "ALREADY_PROCESSED_PAYMENT", "message": "이미 처리된 결제 입니다."}
                """);

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 10000L)))
                .isInstanceOf(TossPaymentException.AlreadyProcessed.class);
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    @DisplayName("에러 코드별로 매핑된 예외가 던져진다.")
    void confirmPayment_WhenErrorResponse_ThrowMappingException(int httpStatus, String code,
                                                                Class<? extends Throwable> expected) {
        enqueue(httpStatus, "{\"code\": \"" + code + "\", \"message\": \"에러 메시지\"}");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 10000L)))
                .isInstanceOf(expected);
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                arguments(400, "ALREADY_PROCESSED_PAYMENT", TossPaymentException.AlreadyProcessed.class),
                arguments(400, "DUPLICATED_ORDER_ID", TossPaymentException.DuplicatedOrder.class),
                arguments(400, "NOT_FOUND_PAYMENT_SESSION", TossPaymentException.SessionExpired.class),
                arguments(400, "INVALID_REQUEST", TossPaymentException.InvalidRequest.class),
                arguments(401, "UNAUTHORIZED_KEY", TossPaymentException.GatewayConfig.class),
                arguments(401, "INVALID_API_KEY", TossPaymentException.GatewayConfig.class),
                arguments(403, "REJECT_CARD_PAYMENT", TossPaymentException.CardRejected.class),
                arguments(404, "NOT_FOUND_PAYMENT", TossPaymentException.PaymentNotFound.class),
                arguments(500, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", TossPaymentException.Retryable.class),
                // 정의되지 않은 코드는 기본 TossPaymentException 으로 떨어진다.
                arguments(400, "SOME_UNDEFINED_CODE", TossPaymentException.class)
        );
    }

}
