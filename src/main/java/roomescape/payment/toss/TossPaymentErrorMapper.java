package roomescape.payment.toss;

import roomescape.payment.exception.CardPaymentRejectedException;
import roomescape.payment.exception.DuplicatedPaymentOrderException;
import roomescape.payment.exception.InvalidPaymentRequestException;
import roomescape.payment.exception.PaymentAlreadyProcessedException;
import roomescape.payment.exception.PaymentGatewayConfigurationException;
import roomescape.payment.exception.PaymentGatewayException;
import roomescape.payment.exception.PaymentNotFoundException;
import roomescape.payment.exception.PaymentSessionExpiredException;
import roomescape.payment.exception.RetryablePaymentException;

final class TossPaymentErrorMapper {

    private TossPaymentErrorMapper() {
    }

    static RuntimeException map(String code) {
        if (code == null) {
            return new PaymentGatewayException();
        }
        return switch (code) {
            case "ALREADY_PROCESSED_PAYMENT" -> new PaymentAlreadyProcessedException();
            case "DUPLICATED_ORDER_ID" -> new DuplicatedPaymentOrderException();
            case "NOT_FOUND_PAYMENT_SESSION" -> new PaymentSessionExpiredException();
            case "INVALID_REQUEST" -> new InvalidPaymentRequestException();
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> new PaymentGatewayConfigurationException();
            case "REJECT_CARD_PAYMENT" -> new CardPaymentRejectedException();
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFoundException();
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new RetryablePaymentException();
            default -> new PaymentGatewayException();
        };
    }
}
