package roomescape.payment.toss;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentResult;

/**
 * 토스 승인 어댑터를 가짜 HTTP 서버(MockWebServer)로 검증한다.
 * base-url을 mock 서버로 덮어, 실제 토스/브라우저 없이 요청·응답 파싱·에러 코드 매핑을 결정적으로 확인한다.
 * (실제 토스 호출은 {@link TossPaymentRealApiTest} 참고.)
 */
@SpringBootTest
@ActiveProfiles("test")
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
    void 승인_성공이면_도메인_결과로_번역해_반환한다() {
        enqueue(200, """
                {
                  "paymentKey": "test_pk_1",
                  "orderId": "order-1",
                  "orderName": "방탈출 예약",
                  "status": "DONE",
                  "totalAmount": 30000,
                  "method": "카드",
                  "approvedAt": "2026-06-17T12:00:00+09:00"
                }
                """);

        PaymentResult result = tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 30000L));

        assertThat(result.paymentKey()).isEqualTo("test_pk_1");
        assertThat(result.orderId()).isEqualTo("order-1");
        assertThat(result.status()).isEqualTo("DONE");
        assertThat(result.totalAmount()).isEqualTo(30000L);
    }

    @Test
    void 에러_상태인데_본문이_비어있어도_부서지지_않고_TossPaymentException으로_변환한다() {
        // 실제로 겪은 케이스: confirm-url 오설정 등으로 에러 status + 빈 본문이 올 때 핸들러가 터지면 안 된다.
        enqueue(404, "");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 30000L)))
                .isInstanceOf(TossPaymentException.class);
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    void 토스_에러코드를_도메인_예외로_매핑한다(int httpStatus, String code, Class<? extends Throwable> expected) {
        enqueue(httpStatus, "{\"code\": \"" + code + "\", \"message\": \"에러 메시지\"}");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 30000L)))
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
                // 정의되지 않은 코드는 기본 TossPaymentException으로 떨어진다(미정의 코드 처리).
                arguments(400, "SOME_UNDEFINED_CODE", TossPaymentException.class)
        );
    }
}
