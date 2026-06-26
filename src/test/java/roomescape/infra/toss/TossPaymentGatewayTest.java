package roomescape.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;

class TossPaymentGatewayTest {

    @Test
    @DisplayName("결제 승인 요청에 주문번호를 멱등키 헤더로 전달한다.")
    void confirmWithIdempotencyKey() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.tosspayments.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TossPaymentGateway gateway = new TossPaymentGateway(builder.build(), new ObjectMapper());
        String response = """
                {
                  "paymentKey": "payment-key",
                  "orderId": "order-1",
                  "status": "DONE",
                  "totalAmount": 50000
                }
                """;
        server.expect(once(), requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(header("Idempotency-Key", "order-1"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        PaymentResult result = gateway.confirm(new PaymentConfirmation("payment-key", "order-1", 50000L));

        assertThat(result.paymentKey()).isEqualTo("payment-key");
        assertThat(result.orderId()).isEqualTo("order-1");
        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(50000L);
        server.verify();
    }
}
