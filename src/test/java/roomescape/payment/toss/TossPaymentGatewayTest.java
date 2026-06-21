package roomescape.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentConfirmation;

class TossPaymentGatewayTest {

    private MockRestServiceServer server;
    private TossPaymentGateway tossPaymentGateway;

    @BeforeEach
    void setUp() {
        String credential = Base64.getEncoder()
                .encodeToString("test_sk_dummy:".getBytes(StandardCharsets.UTF_8));
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credential);
        server = MockRestServiceServer.bindTo(builder).build();
        tossPaymentGateway = new TossPaymentGateway(builder.build(), new ObjectMapper());
    }

    @Test
    void confirm_성공하면_결제_결과를_반환한다() {
        server.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, basicAuthHeader()))
                .andExpect(header("Idempotency-Key", "idempotency-key-1"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "test_payment_key",
                          "orderId": "order-1",
                          "status": "DONE",
                          "totalAmount": 10000
                        }
                        """, MediaType.APPLICATION_JSON));

        var result = tossPaymentGateway.confirm(
                new PaymentConfirmation("test_payment_key", "order-1", 10000L, "idempotency-key-1"));

        assertThat(result.paymentKey()).isEqualTo("test_payment_key");
        assertThat(result.orderId()).isEqualTo("order-1");
        assertThat(result.status()).isEqualTo("DONE");
        assertThat(result.approvedAmount()).isEqualTo(10000L);
        server.verify();
    }

    @ParameterizedTest(name = "{1} -> {2}")
    @MethodSource("errorCases")
    void confirm_에러코드별로_예외를_매핑한다(HttpStatus status, String code, Class<? extends Throwable> expected) {
        server.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andRespond(withStatus(status)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"" + code + "\",\"message\":\"에러 메시지\"}"));

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("test_payment_key", "order-1", 10000L, "idempotency-key-1")))
                .isInstanceOf(expected)
                .hasMessage("에러 메시지");
        server.verify();
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                arguments(HttpStatus.BAD_REQUEST, "ALREADY_PROCESSED_PAYMENT", TossPaymentException.AlreadyProcessed.class),
                arguments(HttpStatus.BAD_REQUEST, "DUPLICATED_ORDER_ID", TossPaymentException.DuplicatedOrder.class),
                arguments(HttpStatus.NOT_FOUND, "NOT_FOUND_PAYMENT_SESSION", TossPaymentException.SessionExpired.class),
                arguments(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", TossPaymentException.InvalidRequest.class),
                arguments(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_KEY", TossPaymentException.GatewayConfig.class),
                arguments(HttpStatus.BAD_REQUEST, "INVALID_API_KEY", TossPaymentException.GatewayConfig.class),
                arguments(HttpStatus.FORBIDDEN, "REJECT_CARD_PAYMENT", TossPaymentException.CardRejected.class),
                arguments(HttpStatus.NOT_FOUND, "NOT_FOUND_PAYMENT", TossPaymentException.PaymentNotFound.class),
                arguments(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", TossPaymentException.Retryable.class),
                arguments(HttpStatus.BAD_REQUEST, "UNDEFINED_TOSS_ERROR", TossPaymentException.class)
        );
    }

    private String basicAuthHeader() {
        String credential = Base64.getEncoder()
                .encodeToString("test_sk_dummy:".getBytes(StandardCharsets.UTF_8));
        return "Basic " + credential;
    }
}
