package roomescape.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.domain.PaymentStatus;
import roomescape.service.payment.PaymentConfirmation;
import roomescape.service.payment.PaymentFailureCategory;
import roomescape.service.payment.PaymentGateway;
import roomescape.service.payment.PaymentGatewayException;
import roomescape.service.payment.PaymentResult;
import roomescape.payment.client.dto.ConfirmRequest;
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
        ConfirmRequest request = new ConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        TossPaymentResponse response;
        try {
            response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.orderId())
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (requestHeaders, clientResponse) -> {
                        TossErrorResponse error = objectMapper.readValue(clientResponse.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.of(clientResponse.getStatusCode(), error);
                    })
                    .body(TossPaymentResponse.class);
        } catch (TossPaymentException e) {
            throw e;
        } catch (RestClientException e) {
            throw new PaymentGatewayException(
                    PaymentFailureCategory.CONFIRMATION_UNKNOWN,
                    "PAYMENT_CONFIRMATION_UNKNOWN",
                    "결제 승인 결과를 확인할 수 없습니다."
            );
        }
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.fromTossStatus(response.status()),
                response.totalAmount()
        );
    }
}
