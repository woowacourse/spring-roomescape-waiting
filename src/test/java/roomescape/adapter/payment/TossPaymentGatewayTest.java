package roomescape.adapter.payment;

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
import roomescape.domain.payment.PaymentStatus;
import roomescape.exception.client.PaymentAlreadyProcessedException;
import roomescape.exception.client.PaymentRejectedException;
import roomescape.exception.server.PaymentGatewayException;

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
        registry.add("toss.secret-key", () -> "test_sk_dummy");
        registry.add("toss.client-key", () -> "test_ck_dummy");
        registry.add("outbound-rate-limit.capacity", () -> "100");
        registry.add("outbound-rate-limit.refill-per-second", () -> "100");
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
                  "totalAmount": 10000,
                  "approvedAt": "2026-06-08T12:00:00+09:00"
                }
                """);

        var result = tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 10000L));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(10000L);
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    void 에러코드별로_매핑된_도메인_예외가_던져진다(int httpStatus, String code, Class<? extends Throwable> expected) {
        enqueue(httpStatus, "{\"code\": \"" + code + "\", \"message\": \"에러 메시지\"}");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 10000L)))
                .isInstanceOf(expected);
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                arguments(400, "ALREADY_PROCESSED_PAYMENT", PaymentAlreadyProcessedException.class),
                arguments(400, "DUPLICATED_ORDER_ID", PaymentRejectedException.class),
                arguments(400, "NOT_FOUND_PAYMENT_SESSION", PaymentRejectedException.class),
                arguments(400, "INVALID_REQUEST", PaymentRejectedException.class),
                arguments(401, "UNAUTHORIZED_KEY", PaymentGatewayException.class),
                arguments(401, "INVALID_API_KEY", PaymentGatewayException.class),
                arguments(403, "REJECT_CARD_PAYMENT", PaymentRejectedException.class),
                arguments(404, "NOT_FOUND_PAYMENT", PaymentRejectedException.class),
                arguments(500, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", PaymentGatewayException.class),
                // 정의되지 않은 코드는 기본 게이트웨이 예외(500)로 떨어진다
                arguments(400, "SOME_UNDEFINED_CODE", PaymentGatewayException.class)
        );
    }
}
