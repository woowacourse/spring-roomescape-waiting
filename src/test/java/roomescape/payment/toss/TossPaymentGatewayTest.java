package roomescape.payment.toss;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TossPaymentGatewayTest {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private MockRestServiceServer server;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.tosspayments.com");
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new TossPaymentGateway(builder.build());
    }

    @Test
    void confirm은_성공_응답을_PaymentResult로_변환한다() {
        server.expect(requestTo(CONFIRM_URL))
                .andExpect(method(POST))
                .andRespond(withSuccess(
                        "{\"paymentKey\":\"pk_1\",\"orderId\":\"order-1\",\"totalAmount\":50000}",
                        MediaType.APPLICATION_JSON
                ));

        PaymentResult result = gateway.confirm(new PaymentConfirmation("pk_1", "order-1", 50_000L));

        assertThat(result.paymentKey()).isEqualTo("pk_1");
        assertThat(result.orderId()).isEqualTo("order-1");
        server.verify();
    }

    @Test
    void confirm은_토스_오류_응답을_TossPaymentException으로_변환한다() {
        server.expect(requestTo(CONFIRM_URL))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"INVALID_REQUEST\",\"message\":\"잘못된 요청입니다.\"}"));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("pk_1", "order-1", 50_000L)))
                .isInstanceOf(TossPaymentException.class)
                .hasMessageContaining("400 BAD_REQUEST");
        server.verify();
    }
}
