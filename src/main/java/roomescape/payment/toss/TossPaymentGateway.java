package roomescape.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.toss.dto.TossErrorResponse;
import roomescape.payment.toss.dto.TossPaymentConfirmRequest;
import roomescape.payment.toss.dto.TossPaymentResponse;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;
    private final TossPaymentErrorMapper errorMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper,
                              TossPaymentErrorMapper errorMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
        this.errorMapper = errorMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    try {
                        TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                        throw errorMapper.map(error);
                    } catch (IOException e) {
                        throw new RoomEscapeException(DomainErrorCode.PAYMENT_FAILED);
                    }
                })
                .body(TossPaymentResponse.class);
        return new PaymentResult(response.paymentKey(), response.orderId(), response.status(), response.totalAmount());
    }
}
