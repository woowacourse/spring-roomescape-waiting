package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import roomescape.domain.PaymentStatus;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentResult;

import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;

class TossPaymentGatewayTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void 토스_결제_승인_API를_호출하고_응답을_결제_결과로_변환한다() throws Exception {
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setBody("""
                        {
                          "paymentKey": "test_payment_key",
                          "orderId": "payment_123456789012345678901",
                          "status": "DONE",
                          "totalAmount": 20000
                        }
                        """));
        TossPaymentGateway gateway = new TossPaymentGateway(RestClient.builder()
                .baseUrl(server.url("/").toString())
                .build(), new ObjectMapper());

        PaymentResult result = gateway.confirm(new PaymentConfirmation(
                "test_payment_key", "payment_123456789012345678901", 20_000L));

        assertThat(result).isEqualTo(new PaymentResult(
                "test_payment_key", "payment_123456789012345678901", PaymentStatus.CONFIRMED, 20_000L));
        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/v1/payments/confirm");
        assertThat(request.getBody().readUtf8())
                .contains("\"paymentKey\":\"test_payment_key\"")
                .contains("\"orderId\":\"payment_123456789012345678901\"")
                .contains("\"amount\":20000");
    }

    @ParameterizedTest
    @MethodSource("errorResponses")
    void 토스_오류를_코드에_맞는_예외로_변환한다(String code, int status,
                                           Class<? extends TossPaymentException> exceptionType)
            throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(status)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setBody("{\"code\":\"%s\",\"message\":\"결제 승인에 실패했습니다.\"}".formatted(code)));
        TossPaymentGateway gateway = new TossPaymentGateway(RestClient.builder()
                .baseUrl(server.url("/").toString())
                .build(), new ObjectMapper());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation(
                        "test_payment_key", "payment_123456789012345678901", 20_000L)))
                .isInstanceOf(exceptionType)
                .hasFieldOrPropertyWithValue("code", code);
    }

    private static Stream<Arguments> errorResponses() {
        return Stream.of(
                Arguments.of("ALREADY_PROCESSED_PAYMENT", 400, TossPaymentException.AlreadyProcessed.class),
                Arguments.of("REJECT_CARD_PAYMENT", 403, TossPaymentException.CardRejected.class),
                Arguments.of("INVALID_API_KEY", 401, TossPaymentException.GatewayConfig.class),
                Arguments.of("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", 500, TossPaymentException.Retryable.class),
                Arguments.of("UNKNOWN_ERROR", 400, TossPaymentException.class)
        );
    }
}
