package roomescape.infrastructure.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentStatus;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.infrastructure.payment.dto.ConfirmRequest;
import roomescape.infrastructure.payment.dto.TossErrorResponse;
import roomescape.infrastructure.payment.dto.TossPaymentResponse;

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
        var request = new ConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount());
        try {
            var response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    //.header("Idempotency-Key", confirmation.idempotencyKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        var error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.of(res.getStatusCode(), error);
                    })
                    .body(TossPaymentResponse.class);
            return toResult(response);
        } catch (TossPaymentException e) {
            throw e;
        } catch (RestClientException e) {
            throw classify(e);
        }
    }

    private RuntimeException classify(RestClientException e) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(e);
        if (rootCause instanceof ConnectException) {
            return new PaymentConnectionException("결제 서버에 연결하지 못했습니다.", e);
        }
        if (rootCause instanceof SocketTimeoutException) {
            return new PaymentUnknownException("결제 결과를 확인하지 못했습니다.", e);
        }
        return e;
    }

    private PaymentResult toResult(TossPaymentResponse response) {
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.from(response.status()),
                response.totalAmount()
        );
    }
}
