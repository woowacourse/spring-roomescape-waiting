package roomescape.infrastructure.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;

class TossRetryInterceptorTest {

    @Test
    @DisplayName("429 응답 시 Retry-After 헤더만큼 대기 후 재시도하여 성공한다.")
    void retryOn429() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        
        RestClient restClient = builder.baseUrl("https://api.tosspayments.com")
                .requestInterceptor(new TossRetryInterceptor(3))
                .build();
        TossPaymentGateway gateway = new TossPaymentGateway(restClient, new ObjectMapper());

        // 첫 번째 시도: 429
        server.expect(times(1), requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "code": "TOO_MANY_REQUESTS",
                                  "message": "Too many requests"
                                }
                                """));

        // 두 번째 시도: 성공
        server.expect(times(1), requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "payment_key",
                          "orderId": "order_123456",
                          "totalAmount": 37000
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = gateway.confirm(new PaymentConfirmation("payment_key", "order_123456", 37_000L, "idempotency-key"));

        assertThat(result.paymentKey()).isEqualTo("payment_key");
        server.verify();
    }

    @Test
    @DisplayName("최대 재시도 횟수를 넘겨도 429면 예외가 발생한다.")
    void failAfterMaxAttempts() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        
        int maxAttempts = 2;
        RestClient restClient = builder.baseUrl("https://api.tosspayments.com")
                .requestInterceptor(new TossRetryInterceptor(maxAttempts))
                .build();
        TossPaymentGateway gateway = new TossPaymentGateway(restClient, new ObjectMapper());

        // 1번(최초) + 2번(재시도) = 총 3번 모두 429
        server.expect(times(maxAttempts + 1), requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "code": "TOO_MANY_REQUESTS",
                                  "message": "Too many requests"
                                }
                                """));

        try {
            gateway.confirm(new PaymentConfirmation("payment_key", "order_123456", 37_000L, "idempotency-key"));
        } catch (TossPaymentException e) {
            assertThat(e.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        }
        
        server.verify();
    }
}
