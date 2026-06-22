package roomescape.payment.infra.toss;

import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;

final class TossPaymentErrorMapper {

    private TossPaymentErrorMapper() {
    }

    static PaymentException map(TossErrorResponse error) {
        PaymentErrorCode errorCode = switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> PaymentErrorCode.ALREADY_PROCESSED;
            case "DUPLICATED_ORDER_ID", "NOT_FOUND_PAYMENT_SESSION", "INVALID_REQUEST" ->
                    PaymentErrorCode.INVALID_REQUEST;
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> PaymentErrorCode.INVALID_API_KEY;
            case "REJECT_CARD_PAYMENT" -> PaymentErrorCode.CARD_REJECTED;
            case "NOT_FOUND_PAYMENT" -> PaymentErrorCode.PAYMENT_NOT_FOUND;
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> PaymentErrorCode.RETRYABLE_ERROR;
            default -> PaymentErrorCode.UNKNOWN_GATEWAY_ERROR;
        };
        return new PaymentException(errorCode, error.code());
    }
}
