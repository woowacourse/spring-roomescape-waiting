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
                PaymentResult result = paymentGateway.confirm(confirmation);
                return confirmationResult(result, orderId, amount);
            } catch (RoomEscapeException exception) {
                if (!isRetryable(exception)) {
                    return PaymentConfirmationResult.failure(exception.code());
                }
                lastRetryableException = exception;
            }
        }
        if (lastRetryableException.code() == DomainErrorCode.PAYMENT_UNKNOWN) {
            return PaymentConfirmationResult.unknownResult();
        }
        return PaymentConfirmationResult.failure(lastRetryableException.code());
    }

    private PaymentConfirmationResult confirmationResult(PaymentResult result, String orderId, Long amount) {
        if (!PaymentStatus.DONE.name().equals(result.status()) || !result.orderId().equals(orderId)) {
            return PaymentConfirmationResult.failure(DomainErrorCode.PAYMENT_FAILED);
        }
        if (!result.approvedAmount().equals(amount)) {
            return PaymentConfirmationResult.failure(DomainErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        return PaymentConfirmationResult.success(result);
    }

    private boolean isRetryable(RoomEscapeException exception) {
        return exception.code() == DomainErrorCode.PAYMENT_RETRYABLE
                || exception.code() == DomainErrorCode.PAYMENT_UNKNOWN;
    }
}
