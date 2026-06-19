package roomescape.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;

class TossPaymentGatewayTest {

    private static final String CONFIRM_URL = "http://localhost/v1/payments/confirm";
    private static final PaymentConfirmation CONFIRMATION =
            new PaymentConfirmation("test_pk_1", "order-1", 10_000L);

    private MockRestServiceServer server;
    private TossPaymentGateway tossPaymentGateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        server = MockRestServiceServer.bindTo(builder).build();
        tossPaymentGateway = new TossPaymentGateway(builder.build(), new ObjectMapper());
    }

    @Test
    @DisplayName("승인에 성공하면 status DONE 과 승인 금액이 매핑된다")
    void confirm_success() {
        server.expect(requestTo(CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "test_pk_1",
                          "orderId": "order-1",
                          "orderName": "방탈출 예약",
                          "status": "DONE",
                          "totalAmount": 10000,
                          "balanceAmount": 10000,
                          "method": "카드"
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = tossPaymentGateway.confirm(CONFIRMATION);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(10_000L);
        assertThat(result.paymentKey()).isEqualTo("test_pk_1");
    }

    @ParameterizedTest(name = "[{0}] {1} -> {2}")
    @MethodSource("errorCases")
    @DisplayName("에러 코드별로 매핑된 도메인 예외가 던져진다")
    void confirm_errorMapping(int httpStatus, String code, Class<? extends Throwable> expected) {
        server.expect(requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatusCode.valueOf(httpStatus))
                        .body("{\"code\": \"" + code + "\", \"message\": \"에러 메시지\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> tossPaymentGateway.confirm(CONFIRMATION))
                .isInstanceOf(expected);
    }

    @Test
    @DisplayName("에러 응답 본문이 비어 있어도 실패를 삼키지 않고 TossPaymentException 으로 던진다")
    void confirm_emptyErrorBody() {
        server.expect(requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatusCode.valueOf(500)));

        assertThatThrownBy(() -> tossPaymentGateway.confirm(CONFIRMATION))
                .isInstanceOf(TossPaymentException.class)
                .extracting(e -> ((TossPaymentException) e).getCode())
                .isEqualTo("UNKNOWN");
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
                // 정의되지 않은 코드는 기본 TossPaymentException 으로 떨어진다.
                arguments(400, "SOME_UNDEFINED_CODE", TossPaymentException.class)
        );
    }
}
