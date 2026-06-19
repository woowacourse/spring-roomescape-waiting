package roomescape.payment.toss;

import org.springframework.stereotype.Component;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.payment.toss.dto.TossErrorResponse;

@Component
public class TossPaymentErrorMapper {

    public RoomEscapeException map(TossErrorResponse error) {
        if (error == null) {
            return new RoomEscapeException(DomainErrorCode.PAYMENT_FAILED);
        }
        return new RoomEscapeException(codeOf(error.code()), error.message());
    }

    private DomainErrorCode codeOf(String code) {
        if (code == null) {
            return DomainErrorCode.PAYMENT_FAILED;
        }
        return switch (code) {
            case "ALREADY_PROCESSED_PAYMENT" -> DomainErrorCode.PAYMENT_ALREADY_PROCESSED;
            case "DUPLICATED_ORDER_ID" -> DomainErrorCode.DUPLICATED_PAYMENT_ORDER;
            case "NOT_FOUND_PAYMENT_SESSION" -> DomainErrorCode.PAYMENT_SESSION_EXPIRED;
            case "INVALID_REQUEST" -> DomainErrorCode.INVALID_PAYMENT_REQUEST;
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> DomainErrorCode.PAYMENT_GATEWAY_CONFIG_ERROR;
            case "REJECT_CARD_PAYMENT" -> DomainErrorCode.PAYMENT_REJECTED;
            case "NOT_FOUND_PAYMENT" -> DomainErrorCode.NOT_FOUND_PAYMENT;
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> DomainErrorCode.PAYMENT_RETRYABLE;
            default -> DomainErrorCode.PAYMENT_FAILED;
        };
    }
}
