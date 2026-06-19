package roomescape.infrastructure.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;

class TossPaymentGatewayTest {

    @Test
    void confirm_RestClient로_승인_API에_바디와_Basic_인증을_전송한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        String secretKey = "test_sk_secret";
        String authorization = "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        RestClient restClient = builder.baseUrl("https://api.tosspayments.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorization)
                .build();
        TossPaymentGateway gateway = new TossPaymentGateway(restClient, new ObjectMapper());
        server.expect(once(), requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", authorization))
                .andExpect(header("Idempotency-Key", "idempotency-key-123"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "paymentKey": "payment_key",
                          "orderId": "order_123456",
                          "amount": 37000
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "payment_key",
                          "orderId": "order_123456",
                          "totalAmount": 37000
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = gateway.confirm(new PaymentConfirmation("payment_key", "order_123456", 37_000L,
                "idempotency-key-123"));

        assertThat(result.paymentKey()).isEqualTo("payment_key");
        assertThat(result.orderId()).isEqualTo("order_123456");
        assertThat(result.amount()).isEqualTo(37_000L);
        server.verify();
    }

    @Test
    void confirm_ALREADY_PROCESSED_PAYMENT는_이미_승인된_결제_예외로_변환한다() {
        assertErrorMapped("ALREADY_PROCESSED_PAYMENT", HttpStatus.BAD_REQUEST,
                TossPaymentException.AlreadyProcessed.class);
    }

    @Test
    void confirm_중복_만료_잘못된_요청은_결제_요청_예외로_변환한다() {
        assertErrorMapped("DUPLICATED_ORDER_ID", HttpStatus.BAD_REQUEST, TossPaymentException.DuplicatedOrder.class);
        assertErrorMapped("NOT_FOUND_PAYMENT_SESSION", HttpStatus.BAD_REQUEST,
                TossPaymentException.SessionExpired.class);
        assertErrorMapped("INVALID_REQUEST", HttpStatus.BAD_REQUEST, TossPaymentException.InvalidRequest.class);
    }

    @Test
    void confirm_키_오류는_결제_키_설정_예외로_변환한다() {
        assertErrorMapped("UNAUTHORIZED_KEY", HttpStatus.UNAUTHORIZED, TossPaymentException.GatewayConfig.class);
        assertErrorMapped("INVALID_API_KEY", HttpStatus.UNAUTHORIZED, TossPaymentException.GatewayConfig.class);
    }

    @Test
    void confirm_카드_거절은_카드_거절_예외로_변환한다() {
        assertErrorMapped("REJECT_CARD_PAYMENT", HttpStatus.FORBIDDEN, TossPaymentException.CardRejected.class);
    }

    @Test
    void confirm_결제건_없음은_결제건_없음_예외로_변환한다() {
        assertErrorMapped("NOT_FOUND_PAYMENT", HttpStatus.NOT_FOUND, TossPaymentException.PaymentNotFound.class);
    }

    @Test
    void confirm_토스_내부_오류는_재시도_가능_예외로_변환한다() {
        assertErrorMapped("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", HttpStatus.INTERNAL_SERVER_ERROR,
                TossPaymentException.Retryable.class);
    }

    @Test
    void confirm_정의되지_않은_코드는_기본_결제_게이트웨이_예외로_변환한다() {
        assertErrorMapped("UNKNOWN_CODE", HttpStatus.BAD_GATEWAY, TossPaymentException.class);
    }

    @Test
    void confirm_연결_실패는_결제_연결_실패_예외로_변환한다() {
        assertNetworkFailureMapped(new ConnectException("Connection refused"),
                TossPaymentException.ConnectionFailed.class,
                "TOSS_CONNECTION_FAILED");
    }

    @Test
    void confirm_연결_타임아웃은_결제_연결_실패_예외로_변환한다() {
        assertNetworkFailureMapped(new SocketTimeoutException("Connect timed out"),
                TossPaymentException.ConnectionFailed.class,
                "TOSS_CONNECTION_FAILED");
    }

    @Test
    void confirm_응답_읽기_타임아웃은_승인_결과_불명확_예외로_변환한다() {
        assertNetworkFailureMapped(new SocketTimeoutException("Read timed out"),
                TossPaymentException.ConfirmationUnknown.class,
                "TOSS_CONFIRMATION_UNKNOWN");
    }

    private void assertErrorMapped(String code, HttpStatus status,
                                   Class<? extends RuntimeException> expectedExceptionType) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("https://api.tosspayments.com").build();
        TossPaymentGateway gateway = new TossPaymentGateway(restClient, new ObjectMapper());
        server.expect(once(), requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andRespond(withStatus(status)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "code": "%s",
                                  "message": "토스 에러 메시지"
                                }
                                """.formatted(code)));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("payment_key", "order_123456", 37_000L,
                "idempotency-key-123")))
                .isInstanceOf(expectedExceptionType);
        server.verify();
    }

    private void assertNetworkFailureMapped(IOException exception,
                                            Class<? extends TossPaymentException> expectedExceptionType,
                                            String expectedCode) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("https://api.tosspayments.com").build();
        TossPaymentGateway gateway = new TossPaymentGateway(restClient, new ObjectMapper());
        server.expect(once(), requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andRespond(withException(exception));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("payment_key", "order_123456", 37_000L,
                "idempotency-key-123")))
                .isInstanceOf(expectedExceptionType)
                .extracting("code")
                .isEqualTo(expectedCode);
        server.verify();
    }
}
