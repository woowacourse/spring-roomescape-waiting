package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentConfirmation;

class TossPaymentGatewayTest {

    private static final PaymentConfirmation CONFIRMATION =
            new PaymentConfirmation("payment-key", "order_id", 5_000L, "idempotency-key");

    @ParameterizedTest(name = "{1} → {2}")
    @MethodSource
    @DisplayName("Toss 에러 응답의 code를 대응하는 도메인 예외로 매핑한다.")
    void mapsTossErrorToDomainException(HttpStatus status, String code, Class<?> expected) {
        TossPaymentGateway gateway = gatewayReturning(status, errorBody(code));

        assertThatThrownBy(() -> gateway.confirm(CONFIRMATION))
                .isInstanceOf(expected);
    }

    static Stream<Arguments> mapsTossErrorToDomainException() {
        return Stream.of(
                arguments(HttpStatus.BAD_REQUEST, "ALREADY_PROCESSED_PAYMENT", TossPaymentException.AlreadyProcessed.class),
                arguments(HttpStatus.BAD_REQUEST, "DUPLICATED_ORDER_ID", TossPaymentException.DuplicatedOrder.class),
                arguments(HttpStatus.BAD_REQUEST, "NOT_FOUND_PAYMENT_SESSION", TossPaymentException.SessionExpired.class),
                arguments(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", TossPaymentException.InvalidRequest.class),
                arguments(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_KEY", TossPaymentException.GatewayConfig.class),
                arguments(HttpStatus.UNAUTHORIZED, "INVALID_API_KEY", TossPaymentException.GatewayConfig.class),
                arguments(HttpStatus.FORBIDDEN, "REJECT_CARD_PAYMENT", TossPaymentException.CardRejected.class),
                arguments(HttpStatus.NOT_FOUND, "NOT_FOUND_PAYMENT", TossPaymentException.PaymentNotFound.class),
                arguments(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", TossPaymentException.Retryable.class)
        );
    }

    @Test
    @DisplayName("정의되지 않은 code는 기본 TossPaymentException으로 떨어진다.")
    void mapsUnknownCodeToBaseException() {
        TossPaymentGateway gateway = gatewayReturning(HttpStatus.BAD_REQUEST, errorBody("SOME_UNDEFINED_CODE"));

        assertThatThrownBy(() -> gateway.confirm(CONFIRMATION))
                .isExactlyInstanceOf(TossPaymentException.class);
    }

    @Test
    @DisplayName("confirm 요청에 주문의 고정 멱등키를 Idempotency-Key 헤더로 보낸다.")
    void sendsIdempotencyKeyHeader() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("/v1/payments/confirm"))
                .andExpect(header("Idempotency-Key", "idempotency-key"))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "order_id",
                          "status": "DONE",
                          "totalAmount": 5000
                        }
                        """, MediaType.APPLICATION_JSON));
        TossPaymentGateway gateway = new TossPaymentGateway(builder.build(), new ObjectMapper());

        gateway.confirm(CONFIRMATION);

        server.verify();
    }

    private TossPaymentGateway gatewayReturning(HttpStatus status, String body) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("/v1/payments/confirm"))
                .andRespond(withStatus(status)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));
        return new TossPaymentGateway(builder.build(), new ObjectMapper());
    }

    private String errorBody(String code) {
        return "{\"code\":\"" + code + "\",\"message\":\"토스 에러 메시지\"}";
    }
}
