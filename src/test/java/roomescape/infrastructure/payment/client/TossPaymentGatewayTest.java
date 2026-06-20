package roomescape.infrastructure.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;
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
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;
import roomescape.exception.code.PaymentErrorCode;
import roomescape.exception.domain.PaymentException;

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
    void confirm이_성공하면_paymentKey를_반환한다() {
        enqueue(200, """
                {
                  "paymentKey": "test_pk_1",
                  "orderId": "order-1",
                  "status": "DONE",
                  "totalAmount": 10000
                }
                """);

        PaymentResult result = tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 10000L));

        assertThat(result.paymentKey()).isEqualTo("test_pk_1");
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    void 에러코드별로_매핑된_PaymentErrorCode가_던져진다(int httpStatus, String tossCode, PaymentErrorCode expected) {
        enqueue(httpStatus, "{\"code\": \"" + tossCode + "\", \"message\": \"에러 메시지\"}");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 10000L)))
                .isInstanceOf(PaymentException.class)
                .satisfies(ex -> assertThat(((PaymentException) ex).getExceptionCode())
                        .isEqualTo(expected));
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                arguments(400, "ALREADY_PROCESSED_PAYMENT", PaymentErrorCode.ALREADY_PROCESSED_PAYMENT),
                arguments(400, "DUPLICATED_ORDER_ID", PaymentErrorCode.DUPLICATED_ORDER_ID),
                arguments(400, "NOT_FOUND_PAYMENT_SESSION", PaymentErrorCode.NOT_FOUND_PAYMENT_SESSION),
                arguments(400, "INVALID_REQUEST", PaymentErrorCode.INVALID_REQUEST),
                arguments(401, "UNAUTHORIZED_KEY", PaymentErrorCode.UNAUTHORIZED_KEY),
                arguments(401, "INVALID_API_KEY", PaymentErrorCode.INVALID_API_KEY),
                arguments(403, "REJECT_CARD_PAYMENT", PaymentErrorCode.REJECT_CARD_PAYMENT),
                arguments(404, "NOT_FOUND_PAYMENT", PaymentErrorCode.NOT_FOUND_PAYMENT),
                arguments(500, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", PaymentErrorCode.FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING),
                arguments(400, "SOME_UNDEFINED_CODE", PaymentErrorCode.PAYMENT_GATEWAY_ERROR)
        );
    }
}
