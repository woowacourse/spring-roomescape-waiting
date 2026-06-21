package roomescape.infrastructure.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;

class TossPaymentGatewayTest {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private MockRestServiceServer server;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.tosspayments.com");
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new TossPaymentGateway(builder.build(), new ObjectMapper());
    }

    @Test
    @DisplayName("confirm 요청에 orderId를 Idempotency-Key 헤더로 보내고 성공 응답을 변환한다")
    void confirmSendsIdempotencyKeyHeader() {
        server.expect(requestTo(CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Idempotency-Key", "order-12345"))
                .andRespond(withSuccess(
                        "{\"paymentKey\":\"pk\",\"orderId\":\"order-12345\",\"status\":\"DONE\",\"totalAmount\":50000}",
                        MediaType.APPLICATION_JSON));

        PaymentResult result = gateway.confirm(new PaymentConfirmation("pk", "order-12345", 50000L));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(50000L);
        server.verify();
    }

    @Test
    @DisplayName("응답 읽기 단계 실패(read timeout)는 PaymentUnknownException으로 표면화된다")
    void readTimeoutBecomesPaymentUnknownException() {
        server.expect(requestTo(CONFIRM_URL))
                .andRespond(request -> {
                    throw new SocketTimeoutException("Read timed out");
                });

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("pk", "order-12345", 50000L)))
                .isInstanceOf(PaymentUnknownException.class);
    }

    @Test
    @DisplayName("연결 단계 실패(connect)는 PaymentConnectionException으로 표면화된다")
    void connectFailureBecomesPaymentConnectionException() {
        server.expect(requestTo(CONFIRM_URL))
                .andRespond(request -> {
                    throw new ConnectException("Connection refused");
                });

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("pk", "order-12345", 50000L)))
                .isInstanceOf(PaymentConnectionException.class);
    }

    @Test
    @DisplayName("토스 에러 응답({code, message})은 TossPaymentException으로 구분되어 처리된다")
    void tossErrorResponseBecomesTossPaymentException() {
        server.expect(requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .body("{\"code\":\"REJECT_CARD_PAYMENT\",\"message\":\"한도초과\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("pk", "order-12345", 50000L)))
                .isInstanceOf(TossPaymentException.class);
    }
}