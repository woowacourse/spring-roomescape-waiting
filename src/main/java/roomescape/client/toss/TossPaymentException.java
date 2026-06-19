package roomescape.client.toss;

import roomescape.client.PaymentAlreadyProcessedException;
import roomescape.client.PaymentException;
import roomescape.client.PaymentFailureException;
import roomescape.client.PaymentGatewayConfigurationException;
import roomescape.client.PaymentGatewayRetryableException;
import roomescape.client.toss.dto.TossErrorResponse;

final class TossPaymentException {

    private TossPaymentException() {
    }

    static PaymentException toDomainException(TossErrorResponse error) {
        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> new PaymentAlreadyProcessedException(error.message());
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" ->
                    new PaymentGatewayConfigurationException(error.code(), error.message());
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" ->
                    new PaymentGatewayRetryableException(error.code(), error.message());
            case "DUPLICATED_ORDER_ID",
                 "NOT_FOUND_PAYMENT_SESSION",
                 "INVALID_REQUEST",
                 "REJECT_CARD_PAYMENT",
                 "NOT_FOUND_PAYMENT" -> new PaymentFailureException(error.code(), error.message());
            default -> new PaymentFailureException(error.code(), error.message());
        };
    }
}
