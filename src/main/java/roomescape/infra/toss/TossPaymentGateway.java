package roomescape.infra.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.infra.toss.dto.TossErrorResponse;
import roomescape.infra.toss.dto.TossPaymentRequest;
import roomescape.infra.toss.dto.TossPaymentResponse;

import java.net.SocketTimeoutException;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final String CONFIRM_PATH = "/v1/payments/confirm";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.restClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Retryable(
            retryFor = { RetryablePaymentException.class },
            noRetryFor = { CustomException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, random = true)
    )
    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossPaymentRequest request = new TossPaymentRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );

        try {
            TossPaymentResponse response = restClient.post()
                    .uri(CONFIRM_PATH)
                    .header("Idempotency-Key", confirmation.orderId())
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (statusCode, clientResponse) -> {
                        TossErrorResponse error = objectMapper.readValue(
                                clientResponse.getBody(), TossErrorResponse.class
                        );
                        throw new CustomException(mapToErrorCode(error));
                    })
                    .body(TossPaymentResponse.class);

            if (response == null) {
                throw new CustomException(ErrorCode.PAYMENT_UNKNOWN_ERROR);
            }
            return new PaymentResult(response.paymentKey(), response.orderId(), response.totalAmount());

        } catch (ResourceAccessException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SocketTimeoutException && cause.getMessage() != null
                    && cause.getMessage().contains("Read")) {
                throw new RetryablePaymentException(ErrorCode.PAYMENT_READ_TIMEOUT);
            }
            throw new RetryablePaymentException(ErrorCode.PAYMENT_CONNECTION_TIMEOUT);
        }
    }

    @Recover
    public PaymentResult recoverConfirm(RetryablePaymentException e, PaymentConfirmation confirmation) {
        throw new CustomException(e.getErrorCode());
    }

    private ErrorCode mapToErrorCode(TossErrorResponse error) {
        if (error == null || error.code() == null) {
            return ErrorCode.PAYMENT_UNKNOWN_ERROR;
        }
        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> ErrorCode.PAYMENT_ALREADY_PROCESSED;
            case "CARD_REJECTED", "EXCEED_MAX_DAILY_PAYMENT_COUNT",
                 "EXCEED_MAX_PAYMENT_AMOUNT" -> ErrorCode.PAYMENT_CARD_REJECTED;
            case "UNAUTHORIZED_KEY", "INVALID_API_SECRET_KEY" -> ErrorCode.PAYMENT_UNAUTHORIZED_KEY;
            case "NOT_FOUND_PAYMENT", "NOT_FOUND_PAYMENT_SESSION" -> ErrorCode.PAYMENT_NOT_FOUND;
            case "TOSS_PAYMENTS_ERROR" -> ErrorCode.PAYMENT_TOSS_INTERNAL_ERROR;
            default -> ErrorCode.PAYMENT_UNKNOWN_ERROR;
        };
    }
}
