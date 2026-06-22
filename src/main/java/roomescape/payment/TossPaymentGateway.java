package roomescape.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.exception.TossPaymentErrorResponse;
import roomescape.exception.TossPaymentException;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TossConfirmRequest(
                        confirmation.paymentKey(),
                        confirmation.orderId(),
                        confirmation.amount()
                ))
                .retrieve()
                .onStatus(status -> status.isError(), (req, res) -> {
                    TossPaymentErrorResponse error;
                    try {
                        error = objectMapper.readValue(res.getBody(), TossPaymentErrorResponse.class);
                    } catch (Exception e) {
                        error = new TossPaymentErrorResponse("UNKNOWN", res.getStatusCode().toString());
                    }
                    throw TossPaymentException.of(res.getStatusCode(), error);
                })
                .body(TossConfirmResponse.class);

        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                response.totalAmount(),
                response.status()
        );
    }

    private record TossConfirmRequest(String paymentKey, String orderId, Long amount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TossConfirmResponse(
            String paymentKey,
            String orderId,
            Long totalAmount,
            String status
    ) {}
}
