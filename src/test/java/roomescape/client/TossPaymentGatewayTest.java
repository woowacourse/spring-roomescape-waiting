package roomescape.client;

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
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.PaymentErrorCode;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentStatus;

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
    void confirm이_성공하면_status가_DONE인_결과를_반환한다() {
        enqueue(200, """
                {
                  "paymentKey": "test_pk_1",
                  "orderId": "order-1",
                  "orderName": "방탈출 예약",
                  "status": "DONE",
                  "totalAmount": 10000,
                  "balanceAmount": 10000,
                  "method": "카드",
                  "approvedAt": "2026-06-23T12:00:00+09:00",
                  "requestedAt": "2026-06-23T11:59:30+09:00"
                }
                """);

        var result = tossPaymentGateway.confirm(new PaymentConfirmation("test_pk_1", "order-1", 10000L));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(10000L);
    }

    @Test
    void 이미_처리된_결제면_ALREADY_PROCESSED_에러코드의_예외가_던져진다() {
        enqueue(400, """
                {"code": "ALREADY_PROCESSED_PAYMENT", "message": "이미 처리된 결제 입니다."}
                """);

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 10000L)))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).getErrorCode())
                        .isEqualTo(PaymentErrorCode.ALREADY_PROCESSED));
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    void 에러코드별로_매핑된_PaymentErrorCode의_예외가_던져진다(
            int httpStatus, String code, PaymentErrorCode expectedErrorCode) {
        enqueue(httpStatus, "{\"code\": \"" + code + "\", \"message\": \"에러 메시지\"}");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 10000L)))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).getErrorCode())
                        .isEqualTo(expectedErrorCode));
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                arguments(400, "ALREADY_PROCESSED_PAYMENT",  PaymentErrorCode.ALREADY_PROCESSED),
                arguments(400, "DUPLICATED_ORDER_ID",         PaymentErrorCode.DUPLICATED_ORDER),
                arguments(400, "NOT_FOUND_PAYMENT_SESSION",   PaymentErrorCode.SESSION_EXPIRED),
                arguments(400, "INVALID_REQUEST",             PaymentErrorCode.INVALID_REQUEST),
                arguments(401, "UNAUTHORIZED_KEY",            PaymentErrorCode.GATEWAY_CONFIG_ERROR),
                arguments(401, "INVALID_API_KEY",             PaymentErrorCode.GATEWAY_CONFIG_ERROR),
                arguments(403, "REJECT_CARD_PAYMENT",         PaymentErrorCode.CARD_REJECTED),
                arguments(404, "NOT_FOUND_PAYMENT",           PaymentErrorCode.NOT_FOUND),
                arguments(500, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", PaymentErrorCode.GATEWAY_INTERNAL_ERROR),
                arguments(400, "SOME_UNDEFINED_CODE",         PaymentErrorCode.UNKNOWN)
        );
    }
}
