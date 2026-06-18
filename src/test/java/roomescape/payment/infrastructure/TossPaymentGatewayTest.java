package roomescape.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.exception.PaymentErrorCode;

class TossPaymentGatewayTest {

    private MockRestServiceServer server;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.tosspayments.com");
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new TossPaymentGateway(builder.build(), new ObjectMapper());
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("ORDER-12345678", "test_payment_key", 1000L);
    }

    @Test
    void 승인에_성공하면_결제_결과를_반환한다() {
        // given
        server.expect(requestTo(endsWith("/v1/payments/confirm")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        """
                        {
                          "paymentKey": "pk_test_1",
                          "orderId": "ORDER-12345678",
                          "status": "DONE",
                          "totalAmount": 1000
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        // when
        PaymentResult result = gateway.confirm(confirmation());

        // then
        assertThat(result.paymentKey()).isEqualTo("pk_test_1");
        assertThat(result.status()).isEqualTo("DONE");
        assertThat(result.totalAmount()).isEqualTo(1000L);
        server.verify();
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @CsvSource({
            "403, REJECT_CARD_PAYMENT, PAYMENT_REJECT_CARD",
            "400, ALREADY_PROCESSED_PAYMENT, PAYMENT_ALREADY_PROCESSED",
            "400, NOT_FOUND_PAYMENT_SESSION, PAYMENT_SESSION_EXPIRED",
            "401, UNAUTHORIZED_KEY, PAYMENT_UNAUTHORIZED_KEY",
            "500, FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING, PAYMENT_PROVIDER_ERROR"
    })
    void 에러_응답을_도메인_예외로_변환한다(int httpStatus, String tossCode, PaymentErrorCode expected) {
        // given
        server.expect(requestTo(endsWith("/v1/payments/confirm")))
                .andRespond(withStatus(HttpStatus.valueOf(httpStatus))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"" + tossCode + "\",\"message\":\"에러 메시지\"}"));

        // when & then
        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(expected);
    }

    @Test
    void 정의되지_않은_에러_코드는_기본_예외로_변환한다() {
        // given
        server.expect(requestTo(endsWith("/v1/payments/confirm")))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"SOME_UNDEFINED_CODE\",\"message\":\"알 수 없는 오류\"}"));

        // when & then
        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_FAILED);
    }
}
