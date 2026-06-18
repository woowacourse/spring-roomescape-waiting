package roomescape.payment.adapter.out.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.payment.adapter.out.toss.dto.TossConfirmRequest;
import roomescape.payment.adapter.out.toss.dto.TossErrorResponse;
import roomescape.payment.adapter.out.toss.dto.TossPaymentResponse;
import roomescape.payment.application.port.out.PaymentGateway;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmRequest request = new TossConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );

        try {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.idempotencyKey())
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        TossErrorResponse error = parseError(res.getBody());
                        throw toDomainException(error);
                    })
                    .body(TossPaymentResponse.class);

            return toResult(response);
        } catch (ResourceAccessException exception) {
            throw toConnectionException(exception);
        } catch (RestClientException exception) {
            throw toConnectionException(exception);
        }
    }

    private TossErrorResponse parseError(java.io.InputStream body) throws IOException {
        return objectMapper.readValue(body, TossErrorResponse.class);
    }

    private EscapeRoomException toDomainException(TossErrorResponse error) {
        ErrorCode errorCode = switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> ErrorCode.PAYMENT_ALREADY_PROCESSED;
            case "DUPLICATED_ORDER_ID", "NOT_FOUND_PAYMENT_SESSION", "INVALID_REQUEST" ->
                    ErrorCode.PAYMENT_INVALID_REQUEST;
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> ErrorCode.PAYMENT_GATEWAY_CONFIG_ERROR;
            case "REJECT_CARD_PAYMENT" -> ErrorCode.PAYMENT_CARD_REJECTED;
            case "NOT_FOUND_PAYMENT" -> ErrorCode.PAYMENT_NOT_FOUND;
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> ErrorCode.PAYMENT_GATEWAY_RETRYABLE;
            case "INVALID_IDEMPOTENCY_KEY" -> ErrorCode.PAYMENT_INVALID_IDEMPOTENCY_KEY;
            case "IDEMPOTENT_REQUEST_PROCESSING" -> ErrorCode.PAYMENT_IDEMPOTENT_REQUEST_PROCESSING;
            default -> ErrorCode.PAYMENT_GATEWAY_ERROR;
        };

        if (errorCode == ErrorCode.PAYMENT_NOT_FOUND) {
            return new EscapeRoomException(errorCode, error.code());
        }
        return new EscapeRoomException(errorCode);
    }

    private EscapeRoomException toConnectionException(RestClientException exception) {
        Throwable rootCause = rootCauseOf(exception);
        if (rootCause instanceof SocketTimeoutException) {
            return new EscapeRoomException(ErrorCode.PAYMENT_GATEWAY_TIMEOUT_UNKNOWN);
        }
        if (rootCause instanceof ConnectException) {
            return new EscapeRoomException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
        }
        return new EscapeRoomException(ErrorCode.PAYMENT_GATEWAY_ERROR);
    }

    private Throwable rootCauseOf(Throwable throwable) {
        Throwable result = throwable;
        while (result.getCause() != null && result.getCause() != result) {
            result = result.getCause();
        }
        return result;
    }

    private PaymentResult toResult(TossPaymentResponse response) {
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                response.status(),
                response.totalAmount()
        );
    }
}
