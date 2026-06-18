package roomescape.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.payment.client.dto.ConfirmRequest;
import roomescape.payment.client.dto.TossErrorResponse;
import roomescape.payment.client.dto.TossPaymentResponse;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentStatus;

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
        try {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.idempotencyKey())
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.of(HttpStatus.valueOf(res.getStatusCode().value()), error);
                    })
                    .body(TossPaymentResponse.class);
            return toResult(response);
        } catch (TossPaymentException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw translate(e);
        } catch (RestClientException e) {
            throw new PaymentTimeoutException("결제 결과를 확인하지 못했습니다. 잠시 후 다시 확인해주세요.");
        }
    }

    private RuntimeException translate(ResourceAccessException e) {
        if (rootCause(e) instanceof ConnectException) {
            return new PaymentConnectionException("결제 서버에 연결하지 못했습니다. 다시 시도해주세요.");
        }
        return new PaymentTimeoutException("결제 결과를 확인하지 못했습니다. 잠시 후 다시 확인해주세요.");
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
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