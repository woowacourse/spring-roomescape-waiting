package roomescape.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
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
        try {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.idempotencyKey())
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        try {
                            TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                            throw errorMapper.map(error);
                        } catch (IOException e) {
                            if (res.getStatusCode().value() == 401) {
                                throw new RoomEscapeException(DomainErrorCode.PAYMENT_GATEWAY_CONFIG_ERROR);
                            }
                            throw new RoomEscapeException(DomainErrorCode.PAYMENT_FAILED);
                        }
                    })
                    .body(TossPaymentResponse.class);
            return new PaymentResult(response.paymentKey(), response.orderId(), response.status(),
                    response.totalAmount());
        } catch (ResourceAccessException e) {
            throw mapAccessException(e);
        } catch (RestClientException e) {
            if (hasCause(e, SocketTimeoutException.class)) {
                throw new RoomEscapeException(DomainErrorCode.PAYMENT_UNKNOWN);
            }
            throw new RoomEscapeException(DomainErrorCode.PAYMENT_RETRYABLE);
        }
    }

    private RoomEscapeException mapAccessException(ResourceAccessException exception) {
        if (hasCause(exception, ConnectException.class)) {
            return new RoomEscapeException(DomainErrorCode.PAYMENT_RETRYABLE);
        }
        if (hasCause(exception, SocketTimeoutException.class)) {
            if (containsMessage(exception, "connect timed out")) {
                return new RoomEscapeException(DomainErrorCode.PAYMENT_RETRYABLE);
            }
            return new RoomEscapeException(DomainErrorCode.PAYMENT_UNKNOWN);
        }
        return new RoomEscapeException(DomainErrorCode.PAYMENT_RETRYABLE);
    }

    private boolean containsMessage(Throwable throwable, String message) {
        Throwable current = throwable;
        while (current != null) {
            String currentMessage = current.getMessage();
            if (currentMessage != null && currentMessage.toLowerCase().contains(message)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
