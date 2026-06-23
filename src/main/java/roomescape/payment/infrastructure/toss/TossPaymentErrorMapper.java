package roomescape.payment.infrastructure.toss;

import roomescape.payment.domain.exception.CardPaymentRejectedException;
import roomescape.payment.domain.exception.DuplicatedPaymentOrderException;
import roomescape.payment.domain.exception.InvalidPaymentRequestException;
import roomescape.payment.domain.exception.PaymentAlreadyProcessedException;
import roomescape.payment.domain.exception.PaymentGatewayConfigurationException;
import roomescape.payment.domain.exception.PaymentGatewayException;
import roomescape.payment.domain.exception.PaymentNotFoundException;
import roomescape.payment.domain.exception.PaymentSessionExpiredException;
import roomescape.payment.domain.exception.RetryablePaymentException;

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
