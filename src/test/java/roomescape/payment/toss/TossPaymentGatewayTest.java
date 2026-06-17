package roomescape.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.config.TossClientConfig;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.payment.PaymentConfirmation;

class TossPaymentGatewayTest {

    private MockWebServer mockWebServer;
    private TossPaymentGateway tossPaymentGateway;

    @BeforeEach
    void beforeEach() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        var restClient = new TossClientConfig().tossRestClient(mockWebServer.url("/").toString(), "test_gsk_dummy",
                Duration.ofSeconds(1), Duration.ofSeconds(1));
        tossPaymentGateway = new TossPaymentGateway(restClient, new ObjectMapper(), new TossPaymentErrorMapper());
    }

    @AfterEach
    void afterEach() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void confirmSendsTossShapeAndReturnsResultTest() throws InterruptedException {
        enqueue(200, """
                {"paymentKey":"payment_key","orderId":"order_test","status":"DONE","totalAmount":50000}
                """);

        var result = tossPaymentGateway.confirm(new PaymentConfirmation("payment_key", "order_test", "order_test", 50000L));
        var request = mockWebServer.takeRequest();

        assertThat(result.status()).isEqualTo("DONE");
        assertThat(result.approvedAmount()).isEqualTo(50000L);
        assertThat(request.getPath()).isEqualTo("/v1/payments/confirm");
        assertThat(request.getHeader("Authorization")).isEqualTo(expectedAuthorization());
        assertThat(request.getHeader("Idempotency-Key")).isEqualTo("order_test");
        assertThat(request.getBody().readUtf8()).contains("payment_key", "order_test", "50000");
    }

    @ParameterizedTest
    @MethodSource("errorCases")
    void tossErrorMapsToDomainErrorTest(int status, String code, DomainErrorCode expected) {
        enqueue(status, "{\"code\":\"" + code + "\",\"message\":\"error\"}");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("payment_key", "order_test", "order_test", 50000L)))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(expected));
    }

    @Test
    void tossErrorWithoutCodeMapsToPaymentFailedTest() {
        enqueue(400, "{}");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("payment_key", "order_test", "order_test", 50000L)))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code())
                        .isEqualTo(DomainErrorCode.PAYMENT_FAILED));
    }

    @Test
    void readTimeoutMapsToUnknownPaymentErrorTest() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"paymentKey\":\"payment_key\",\"orderId\":\"order_test\",\"status\":\"DONE\",\"totalAmount\":50000}")
                .setBodyDelay(2, TimeUnit.SECONDS));

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("payment_key", "order_test", "order_test", 50000L)))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code())
                        .isEqualTo(DomainErrorCode.PAYMENT_UNKNOWN));
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                arguments(400, "ALREADY_PROCESSED_PAYMENT", DomainErrorCode.PAYMENT_ALREADY_PROCESSED),
                arguments(403, "REJECT_CARD_PAYMENT", DomainErrorCode.PAYMENT_REJECTED),
                arguments(401, "INVALID_API_KEY", DomainErrorCode.PAYMENT_GATEWAY_CONFIG_ERROR),
                arguments(500, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", DomainErrorCode.PAYMENT_RETRYABLE),
                arguments(400, "SOME_UNDEFINED_CODE", DomainErrorCode.PAYMENT_FAILED)
        );
    }

    private void enqueue(int statusCode, String body) {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(statusCode)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }

    private String expectedAuthorization() {
        String basic = Base64.getEncoder()
                .encodeToString("test_gsk_dummy:".getBytes(StandardCharsets.UTF_8));
        return "Basic " + basic;
    }
}
