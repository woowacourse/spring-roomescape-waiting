package roomescape.infrastructure.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
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

    private static PaymentConfirmation testConfirmation() {
        return new PaymentConfirmation("test_pk_1", "order-1", 10000L);
    }

    private void enqueue(int statusCode, String body) {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(statusCode)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }

    private TossPaymentGateway gatewayWithTimeout(int connectTimeoutMs, int readTimeoutMs) {
        String basic = Base64.getEncoder()
                .encodeToString("test_gsk_dummy:".getBytes(StandardCharsets.UTF_8));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        RestClient client = RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .defaultHeader("Authorization", "Basic " + basic)
                .requestFactory(factory)
                .build();
        return new TossPaymentGateway(client, new ObjectMapper());
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

        PaymentResult result = tossPaymentGateway.confirm(testConfirmation());

        assertThat(result.paymentKey()).isEqualTo("test_pk_1");
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    void 에러코드별로_매핑된_PaymentErrorCode가_던져진다(int httpStatus, String tossCode, PaymentErrorCode expected) {
        enqueue(httpStatus, "{\"code\": \"" + tossCode + "\", \"message\": \"에러 메시지\"}");

        assertThatThrownBy(() -> tossPaymentGateway.confirm(testConfirmation()))
                .isInstanceOf(PaymentException.class)
                .satisfies(ex -> assertThat(((PaymentException) ex).getExceptionCode())
                        .isEqualTo(expected));
    }

    @Test
    void read_timeout_발생_시_PAYMENT_READ_TIMEOUT_예외가_던져진다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBodyDelay(2, TimeUnit.SECONDS)
                .setBody("{\"paymentKey\":\"pk\",\"orderId\":\"order-1\",\"status\":\"DONE\",\"totalAmount\":10000}"));

        TossPaymentGateway gateway = gatewayWithTimeout(3000, 500);

        assertThatThrownBy(() -> gateway.confirm(testConfirmation()))
                .isInstanceOf(PaymentException.class)
                .satisfies(ex -> assertThat(((PaymentException) ex).getExceptionCode())
                        .isEqualTo(PaymentErrorCode.PAYMENT_READ_TIMEOUT));
    }

    @Test
    void 서버_연결_거부_시_PAYMENT_CONNECT_FAILED_예외가_던져진다() throws IOException {
        MockWebServer closedServer = new MockWebServer();
        closedServer.start();
        String closedUrl = closedServer.url("/").toString();
        closedServer.shutdown();

        String basic = Base64.getEncoder()
                .encodeToString("test_gsk_dummy:".getBytes(StandardCharsets.UTF_8));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(500);
        factory.setReadTimeout(500);
        RestClient closedClient = RestClient.builder()
                .baseUrl(closedUrl)
                .defaultHeader("Authorization", "Basic " + basic)
                .requestFactory(factory)
                .build();
        TossPaymentGateway gateway = new TossPaymentGateway(closedClient, new ObjectMapper());

        assertThatThrownBy(() -> gateway.confirm(testConfirmation()))
                .isInstanceOf(PaymentException.class)
                .satisfies(ex -> assertThat(((PaymentException) ex).getExceptionCode())
                        .isEqualTo(PaymentErrorCode.PAYMENT_CONNECT_FAILED));
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                arguments(400, "ALREADY_PROCESSED_PAYMENT", PaymentErrorCode.ALREADY_PROCESSED_PAYMENT),
                arguments(400, "DUPLICATED_ORDER_ID", PaymentErrorCode.DUPLICATED_ORDER_ID),
                arguments(400, "NOT_FOUND_PAYMENT_SESSION", PaymentErrorCode.NOT_FOUND_PAYMENT_SESSION),
                arguments(400, "INVALID_REQUEST", PaymentErrorCode.INVALID_REQUEST),
                arguments(400, "UNAUTHORIZED_KEY", PaymentErrorCode.UNAUTHORIZED_KEY),
                arguments(400, "INVALID_API_KEY", PaymentErrorCode.INVALID_API_KEY),
                arguments(403, "REJECT_CARD_PAYMENT", PaymentErrorCode.REJECT_CARD_PAYMENT),
                arguments(404, "NOT_FOUND_PAYMENT", PaymentErrorCode.NOT_FOUND_PAYMENT),
                arguments(500, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", PaymentErrorCode.FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING),
                arguments(400, "SOME_UNDEFINED_CODE", PaymentErrorCode.PAYMENT_GATEWAY_ERROR)
        );
    }
}
