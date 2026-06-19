package roomescape.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import roomescape.payment.*;
import roomescape.payment.client.dto.TossConfirmRequest;
import roomescape.payment.client.dto.TossErrorResponse;
import roomescape.payment.client.dto.TossPaymentResponse;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        try {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .body(new TossConfirmRequest(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount()))
                    .retrieve()
                    .body(TossPaymentResponse.class);
            return response.toResult();
        } catch (RestClientResponseException e) {
            TossErrorResponse error = parseError(e.getResponseBodyAsString());
            throw new TossPaymentException(error.message());
        }
    }

    private TossErrorResponse parseError(String body) {
        try {
            return objectMapper.readValue(body, TossErrorResponse.class);
        } catch (Exception e) {
            return new TossErrorResponse("UNKNOWN", "결제 처리 중 오류가 발생했습니다.");
        }
    }
}
