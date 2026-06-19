package roomescape.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentStatus;

class TossPaymentGatewayTest {

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void confirm이_성공하면_DONE을_반환한다() {
        enqueue(200, """
                {
                  "paymentKey": "test_pk_1",
                  "orderId": "order_1",
                  "status": "DONE",
                  "totalAmount": 10000
                }
                """);

        TossPaymentGateway gateway = gateway();

        var result = gateway.confirm(new PaymentConfirmation("test_pk_1", "order_1", 10000L));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(10000L);
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    void 에러코드별로_매핑된_예외가_던져진다(int httpStatus, String code, Class<? extends Throwable> expected) {
        enqueue(httpStatus, "{\"code\": \"" + code + "\", \"message\": \"에러 메시지\"}");

        TossPaymentGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("test_pk_1", "order_1", 10000L)))
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
                arguments(400, "SOME_UNDEFINED_CODE", TossPaymentException.class)
        );
    }

    private TossPaymentGateway gateway() {
        RestClient restClient = RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpf")
                .build();
        return new TossPaymentGateway(restClient, new ObjectMapper());
    }

    private void enqueue(int statusCode, String body) {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(statusCode)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }
}
