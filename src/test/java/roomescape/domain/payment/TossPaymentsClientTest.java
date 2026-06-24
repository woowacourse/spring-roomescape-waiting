package roomescape.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.dto.PaymentConfirmRequest;
import roomescape.domain.payment.dto.PaymentConfirmResponse;

class TossPaymentsClientTest {

    private static final String BASE_URL = "https://api.tosspayments.com";
    private static final String SECRET_KEY = "test_sk_dummy";

    @Test
    void 토스_결제_승인_API를_호출한다() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        TossPaymentsClient client = new TossPaymentsClient(
            restClientBuilder,
            new ObjectMapper(),
            BASE_URL,
            SECRET_KEY
        );
        PaymentConfirmRequest request = new PaymentConfirmRequest("paymentKey", "orderId", 1000L);

        server.expect(once(), requestTo(BASE_URL + "/v1/payments/confirm"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, authorizationHeader()))
            .andExpect(content().json("""
                {
                  "paymentKey": "paymentKey",
                  "orderId": "orderId",
                  "amount": 1000
                }
                """))
            .andRespond(withSuccess("""
                {
                  "paymentKey": "paymentKey",
                  "orderId": "orderId",
                  "totalAmount": 1000,
                  "status": "DONE"
                }
                """, MediaType.APPLICATION_JSON));

        PaymentConfirmResponse response = client.confirm(request);

        assertThat(response.status()).isEqualTo("DONE");
        assertThat(response.totalAmount()).isEqualTo(1000L);
        server.verify();
    }

    @Test
    void 토스_에러_응답의_code와_message를_예외에_담는다() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        TossPaymentsClient client = new TossPaymentsClient(
            restClientBuilder,
            new ObjectMapper(),
            BASE_URL,
            SECRET_KEY
        );
        PaymentConfirmRequest request = new PaymentConfirmRequest("paymentKey", "orderId", 1000L);

        server.expect(once(), requestTo(BASE_URL + "/v1/payments/confirm"))
            .andRespond(withBadRequest().body("""
                {
                  "code": "ALREADY_PROCESSED_PAYMENT",
                  "message": "이미 처리된 결제 입니다."
                }
                """).contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.confirm(request))
            .isInstanceOf(PaymentException.class)
            .extracting("code")
            .isEqualTo("ALREADY_PROCESSED_PAYMENT");
        server.verify();
    }

    private String authorizationHeader() {
        String encodedSecretKey = Base64.getEncoder()
            .encodeToString((SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedSecretKey;
    }
}
