package roomescape.payment.infrastructure.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
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
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.domain.exception.CardPaymentRejectedException;
import roomescape.payment.domain.exception.DuplicatedPaymentOrderException;
import roomescape.payment.domain.exception.InvalidPaymentRequestException;
import roomescape.payment.domain.exception.PaymentAlreadyProcessedException;
import roomescape.payment.domain.exception.PaymentGatewayConfigurationException;
import roomescape.payment.domain.exception.PaymentGatewayException;
import roomescape.payment.domain.exception.PaymentNotFoundException;
import roomescape.payment.domain.exception.PaymentSessionExpiredException;
import roomescape.payment.domain.exception.RetryablePaymentException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TossPaymentGatewayTest {

    private static final String SECRET_KEY = "test_sk_dummy";
    private static final MockWebServer MOCK_WEB_SERVER = startServer();

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.toss.base-url", () -> MOCK_WEB_SERVER.url("/").toString());
        registry.add("payment.toss.secret-key", () -> SECRET_KEY);
    }

    @AfterAll
    static void tearDown() throws IOException {
        MOCK_WEB_SERVER.shutdown();
    }

    @Test
    void confirm은_Basic_인증과_세_필드_JSON으로_승인_API를_호출한다() throws InterruptedException {
        enqueue(200, """
                {
                  "paymentKey": "payment-key",
                  "orderId": "order-123456",
                  "status": "DONE",
                  "totalAmount": 50000
                }
                """);

        var result = tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-123456", 50_000L));
        RecordedRequest request = MOCK_WEB_SERVER.takeRequest();

        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/v1/payments/confirm");
        assertThat(request.getHeader("Content-Type")).startsWith("application/json");
        String expectedCredential = Base64.getEncoder()
                .encodeToString((SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));
        assertThat(request.getHeader("Authorization")).isEqualTo("Basic " + expectedCredential);
        assertThat(request.getBody().readUtf8())
                .contains("\"paymentKey\":\"payment-key\"")
                .contains("\"orderId\":\"order-123456\"")
                .contains("\"amount\":50000");
        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(50_000L);
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    void Toss_에러코드를_도메인_예외로_변환한다(
            int httpStatus,
            String code,
            Class<? extends Throwable> expectedException
    ) {
        enqueue(httpStatus, "{\"code\":\"" + code + "\",\"message\":\"외부 오류\"}");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-123456", 50_000L)))
                .isInstanceOf(expectedException);
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                arguments(400, "ALREADY_PROCESSED_PAYMENT", PaymentAlreadyProcessedException.class),
                arguments(400, "DUPLICATED_ORDER_ID", DuplicatedPaymentOrderException.class),
                arguments(400, "NOT_FOUND_PAYMENT_SESSION", PaymentSessionExpiredException.class),
                arguments(400, "INVALID_REQUEST", InvalidPaymentRequestException.class),
                arguments(401, "UNAUTHORIZED_KEY", PaymentGatewayConfigurationException.class),
                arguments(401, "INVALID_API_KEY", PaymentGatewayConfigurationException.class),
                arguments(403, "REJECT_CARD_PAYMENT", CardPaymentRejectedException.class),
                arguments(404, "NOT_FOUND_PAYMENT", PaymentNotFoundException.class),
                arguments(500, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", RetryablePaymentException.class),
                arguments(400, "UNDEFINED_CODE", PaymentGatewayException.class)
        );
    }

    private static MockWebServer startServer() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
            return server;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void enqueue(int statusCode, String body) {
        MOCK_WEB_SERVER.enqueue(new MockResponse()
                .setResponseCode(statusCode)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }
}
