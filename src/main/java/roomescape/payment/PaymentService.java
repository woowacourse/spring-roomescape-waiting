package roomescape.payment;

import org.springframework.stereotype.Service;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;

@Service
public class PaymentService {

    private static final int MAX_CONFIRM_ATTEMPTS = 3;

    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public PaymentConfirmationResult confirm(String paymentKey, String orderId, String idempotencyKey, Long amount) {
        PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, idempotencyKey, amount);
        RoomEscapeException lastRetryableException = null;

        for (int attempt = 1; attempt <= MAX_CONFIRM_ATTEMPTS; attempt++) {
            try {
                return PaymentConfirmationResult.success(paymentGateway.confirm(confirmation));
            } catch (RoomEscapeException exception) {
                if (!isRetryable(exception)) {
                    throw exception;
                }
                lastRetryableException = exception;
            }
        }
        if (lastRetryableException.code() == DomainErrorCode.PAYMENT_UNKNOWN) {
            return PaymentConfirmationResult.unknownResult();
        }
        throw lastRetryableException;
    }

    private boolean isRetryable(RoomEscapeException exception) {
        return exception.code() == DomainErrorCode.PAYMENT_RETRYABLE
                || exception.code() == DomainErrorCode.PAYMENT_UNKNOWN;
    }
}
