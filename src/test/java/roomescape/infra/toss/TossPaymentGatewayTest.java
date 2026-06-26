package roomescape.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;

class TossPaymentGatewayTest {

    @Test
    @DisplayName("결제 승인 요청에 주문 번호를 Idempotency-Key 헤더로 전송한다.")
    void 멱등키_헤더_전송() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.tosspayments.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TossPaymentGateway gateway = new TossPaymentGateway(builder.build(), new ObjectMapper());

        server.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Idempotency-Key", "order-1"))
                .andExpect(content().json("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "order-1",
                          "amount": 50000
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "order-1",
                          "status": "DONE",
                          "totalAmount": 50000
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = gateway.confirm(new PaymentConfirmation("payment-key", "order-1", 50000L));

        assertThat(result.paymentKey()).isEqualTo("payment-key");
        assertThat(result.orderId()).isEqualTo("order-1");
        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(50000L);
        server.verify();
    }
}
