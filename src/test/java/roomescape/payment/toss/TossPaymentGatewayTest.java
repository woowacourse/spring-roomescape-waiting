package roomescape.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
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
import roomescape.payment.PaymentResultUnknownException;

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
        // 타임아웃 테스트를 빠르게 돌리기 위해 짧게 둔다(즉시 응답하는 다른 테스트엔 영향 없음).
        registry.add("toss.connect-timeout", () -> "500ms");
        registry.add("toss.read-timeout", () -> "300ms");
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

    private void drainRecordedRequests() throws InterruptedException {
        while (mockWebServer.takeRequest(0, TimeUnit.MILLISECONDS) != null) {
            // 공유 MockWebServer라 이전 테스트가 남긴 요청 기록을 비운다.
        }
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
                new PaymentConfirmation("test_pk_1", "order-1", 30000L, "idem-1"));

        assertThat(result.paymentKey()).isEqualTo("test_pk_1");
        assertThat(result.orderId()).isEqualTo("order-1");
        assertThat(result.status()).isEqualTo("DONE");
        assertThat(result.totalAmount()).isEqualTo(30000L);
    }

    @Test
    void confirm_요청에_주문의_Idempotency_Key를_헤더로_싣는다() throws InterruptedException {
        drainRecordedRequests();
        enqueue(200, """
                {"paymentKey": "test_pk_1", "orderId": "order-1", "status": "DONE", "totalAmount": 30000}
                """);

        tossPaymentGateway.confirm(new PaymentConfirmation("test_pk_1", "order-1", 30000L, "idem-key-42"));

        RecordedRequest recorded = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getHeader("Idempotency-Key")).isEqualTo("idem-key-42");
    }

    @Test
    void 에러_상태인데_본문이_비어있어도_부서지지_않고_TossPaymentException으로_변환한다() {
        // 실제로 겪은 케이스: confirm-url 오설정 등으로 에러 status + 빈 본문이 올 때 핸들러가 터지면 안 된다.
        enqueue(404, "");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 30000L, "idem-1")))
                .isInstanceOf(TossPaymentException.class);
    }

    @Test
    void read_timeout이면_결과를_모르는_예외로_번역한다() {
        // connect는 성공(서버는 떠 있음)하지만 응답을 보내지 않아 read timeout을 유발한다.
        // 결과가 불명확하므로 '확실히 안 됨'이 아니라 '모름'(PaymentResultUnknownException)으로 떨어져야 한다.
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 30000L, "idem-1")))
                .isInstanceOf(PaymentResultUnknownException.class);
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    void 토스_에러코드를_도메인_예외로_매핑한다(int httpStatus, String code, Class<? extends Throwable> expected) {
        enqueue(httpStatus, "{\"code\": \"" + code + "\", \"message\": \"에러 메시지\"}");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_pk_1", "order-1", 30000L, "idem-1")))
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
