package roomescape.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;

class TossPaymentGatewayTest {

    private static final String BASE_URL = "https://api.tosspayments.com";
    private static final String CONFIRM_URL = BASE_URL + "/v1/payments/confirm";

    private MockRestServiceServer mockServer;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        gateway = new TossPaymentGateway(builder.build(), new ObjectMapper());
    }

    @Test
    @DisplayName("정상 승인 응답 시 PaymentResult를 반환한다.")
    void confirm_success_returnsPaymentResult() {
        mockServer.expect(requestTo(CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"paymentKey":"pay-key","orderId":"order-123","totalAmount":10000}
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = gateway.confirm(new PaymentConfirmation("pay-key", "order-123", 10000L));

        assertThat(result.paymentKey()).isEqualTo("pay-key");
        assertThat(result.orderId()).isEqualTo("order-123");
        assertThat(result.amount()).isEqualTo(10000L);
        mockServer.verify();
    }

    @Test
    @DisplayName("ALREADY_PROCESSED_PAYMENT 에러 코드는 PAYMENT_ALREADY_PROCESSED 예외로 변환된다.")
    void confirm_alreadyProcessed_throwsPaymentAlreadyProcessed() {
        mockServer.expect(requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"code":"ALREADY_PROCESSED_PAYMENT","message":"이미 처리된 결제입니다."}
                                """));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("pay-key", "order-123", 10000L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_PROCESSED);
    }

    @Test
    @DisplayName("CARD_REJECTED 에러 코드는 PAYMENT_CARD_REJECTED 예외로 변환된다.")
    void confirm_cardRejected_throwsPaymentCardRejected() {
        mockServer.expect(requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"code":"CARD_REJECTED","message":"카드 결제가 거절되었습니다."}
                                """));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("pay-key", "order-123", 10000L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_CARD_REJECTED);
    }

    @Test
    @DisplayName("UNAUTHORIZED_KEY 에러 코드는 PAYMENT_UNAUTHORIZED_KEY 예외로 변환된다.")
    void confirm_unauthorizedKey_throwsPaymentUnauthorizedKey() {
        mockServer.expect(requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"code":"UNAUTHORIZED_KEY","message":"인증되지 않은 키입니다."}
                                """));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("pay-key", "order-123", 10000L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_UNAUTHORIZED_KEY);
    }

    @Test
    @DisplayName("5xx 서버 오류는 PAYMENT_TOSS_INTERNAL_ERROR 예외로 변환된다.")
    void confirm_serverError_throwsPaymentTossInternalError() {
        mockServer.expect(requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"code":"TOSS_PAYMENTS_ERROR","message":"Toss 서버 오류입니다."}
                                """));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("pay-key", "order-123", 10000L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_TOSS_INTERNAL_ERROR);
    }

    @Test
    @DisplayName("매핑되지 않은 에러 코드는 PAYMENT_UNKNOWN_ERROR 예외로 변환된다.")
    void confirm_unknownErrorCode_throwsPaymentUnknownError() {
        mockServer.expect(requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"code":"SOME_UNKNOWN_CODE","message":"알 수 없는 오류입니다."}
                                """));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("pay-key", "order-123", 10000L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_UNKNOWN_ERROR);
    }
}
