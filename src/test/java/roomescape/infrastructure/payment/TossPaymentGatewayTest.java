package roomescape.infrastructure.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
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
        TossPaymentGateway gateway = new TossPaymentGateway(builder, "https://api.tosspayments.com", secretKey);
        server.expect(once(), requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", authorization))
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

        PaymentResult result = gateway.confirm(new PaymentConfirmation("payment_key", "order_123456", 37_000L));

        assertThat(result.paymentKey()).isEqualTo("payment_key");
        assertThat(result.orderId()).isEqualTo("order_123456");
        assertThat(result.amount()).isEqualTo(37_000L);
        server.verify();
    }
}
