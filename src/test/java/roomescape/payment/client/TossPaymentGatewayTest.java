package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentStatus;

class TossPaymentGatewayTest {

    private static final String BASE_URL = "https://toss.test";
    private static final String CONFIRM_URI = BASE_URL + "/v1/payments/confirm";

    private TossPaymentGateway gatewayResponding(HttpStatus status, String body) {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(CONFIRM_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(status)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));
        return new TossPaymentGateway(builder.build(), new ObjectMapper());
    }

    @Test
    void confirm이_성공하면_status가_DONE인_결과를_반환한다() {
        TossPaymentGateway gateway = gatewayResponding(HttpStatus.OK, """
                {
                  "paymentKey": "test_pk_1",
                  "orderId": "order-1",
                  "orderName": "방탈출 예약",
                  "status": "DONE",
                  "totalAmount": 10000,
                  "balanceAmount": 10000,
                  "method": "카드",
                  "approvedAt": "2026-06-08T12:00:00+09:00",
                  "requestedAt": "2026-06-08T11:59:30+09:00"
                }
                """);

        var result = gateway.confirm(new PaymentConfirmation("test_pk_1", "order-1", 10000L));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(10000L);
    }

    @Test
    void 이미_처리된_결제면_AlreadyProcessed가_던져진다() {
        TossPaymentGateway gateway = gatewayResponding(HttpStatus.BAD_REQUEST, """
                {"code": "ALREADY_PROCESSED_PAYMENT", "message": "이미 처리된 결제 입니다."}
                """);

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("test_pk_1", "order-1", 10000L)))
                .isInstanceOf(TossPaymentException.AlreadyProcessed.class);
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    void 에러코드별로_매핑된_예외가_던져진다(HttpStatus httpStatus, String code, Class<? extends Throwable> expected) {
        TossPaymentGateway gateway = gatewayResponding(httpStatus,
                "{\"code\": \"" + code + "\", \"message\": \"에러 메시지\"}");

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("test_pk_1", "order-1", 10000L)))
                .isInstanceOf(expected);
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                arguments(HttpStatus.BAD_REQUEST, "ALREADY_PROCESSED_PAYMENT", TossPaymentException.AlreadyProcessed.class),
                arguments(HttpStatus.BAD_REQUEST, "DUPLICATED_ORDER_ID", TossPaymentException.DuplicatedOrder.class),
                arguments(HttpStatus.BAD_REQUEST, "NOT_FOUND_PAYMENT_SESSION", TossPaymentException.SessionExpired.class),
                arguments(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", TossPaymentException.InvalidRequest.class),
                arguments(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_KEY", TossPaymentException.GatewayConfig.class),
                arguments(HttpStatus.UNAUTHORIZED, "INVALID_API_KEY", TossPaymentException.GatewayConfig.class),
                arguments(HttpStatus.FORBIDDEN, "REJECT_CARD_PAYMENT", TossPaymentException.CardRejected.class),
                arguments(HttpStatus.NOT_FOUND, "NOT_FOUND_PAYMENT", TossPaymentException.PaymentNotFound.class),
                arguments(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", TossPaymentException.Retryable.class),
                arguments(HttpStatus.BAD_REQUEST, "SOME_UNDEFINED_CODE", TossPaymentException.class)
        );
    }
}
