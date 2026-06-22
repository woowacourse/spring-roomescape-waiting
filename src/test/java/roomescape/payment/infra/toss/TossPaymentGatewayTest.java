package roomescape.payment.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;

class TossPaymentGatewayTest {

    @Test
    void UTF_8_Basic_인증과_승인_요청을_전송한다() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.tosspayments.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TossPaymentGateway gateway = gateway(builder);
        String authorization = "Basic " + Base64.getEncoder()
                .encodeToString("test_sk_시크릿:".getBytes(StandardCharsets.UTF_8));

        server.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", authorization))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "ROOM_order123",
                          "amount": 10000
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "ROOM_order123",
                          "totalAmount": 10000,
                          "status": "DONE"
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = gateway.confirm(
                new PaymentConfirmation("payment-key", "ROOM_order123", 10_000L));

        assertThat(result).isEqualTo(
                new PaymentResult("payment-key", "ROOM_order123", 10_000L, "DONE"));
        server.verify();
    }

    @ParameterizedTest
    @MethodSource("errorMappings")
    void Toss_에러코드를_도메인_예외로_변환한다(String tossCode, PaymentErrorCode expected) {
        PaymentException exception = TossPaymentErrorMapper.map(
                new TossErrorResponse(tossCode, "gateway message"));

        assertThat(exception.errorCode()).isEqualTo(expected);
        assertThat(exception.gatewayCode()).isEqualTo(tossCode);
    }

    @Test
    void 미정의_코드는_기본_게이트웨이_예외로_변환한다() {
        assertThatThrownBy(() -> {
            throw TossPaymentErrorMapper.map(new TossErrorResponse("NEW_UNKNOWN_CODE", "unknown"));
        })
                .isInstanceOf(PaymentException.class)
                .extracting(exception -> ((PaymentException) exception).errorCode())
                .isEqualTo(PaymentErrorCode.UNKNOWN_GATEWAY_ERROR);
    }

    private TossPaymentGateway gateway(RestClient.Builder builder) {
        return new TossPaymentGateway(
                builder.build(),
                new ObjectMapper(),
                new PaymentProperties(
                        new PaymentProperties.Toss(
                                "https://api.tosspayments.com",
                                "test_ck_test",
                                "test_sk_시크릿"
                        ),
                        10_000L
                )
        );
    }

    private static Stream<Arguments> errorMappings() {
        return Stream.of(
                Arguments.of("ALREADY_PROCESSED_PAYMENT", PaymentErrorCode.ALREADY_PROCESSED),
                Arguments.of("DUPLICATED_ORDER_ID", PaymentErrorCode.INVALID_REQUEST),
                Arguments.of("NOT_FOUND_PAYMENT_SESSION", PaymentErrorCode.INVALID_REQUEST),
                Arguments.of("INVALID_REQUEST", PaymentErrorCode.INVALID_REQUEST),
                Arguments.of("UNAUTHORIZED_KEY", PaymentErrorCode.INVALID_API_KEY),
                Arguments.of("INVALID_API_KEY", PaymentErrorCode.INVALID_API_KEY),
                Arguments.of("REJECT_CARD_PAYMENT", PaymentErrorCode.CARD_REJECTED),
                Arguments.of("NOT_FOUND_PAYMENT", PaymentErrorCode.PAYMENT_NOT_FOUND),
                Arguments.of(
                        "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
                        PaymentErrorCode.RETRYABLE_ERROR
                )
        );
    }
}
