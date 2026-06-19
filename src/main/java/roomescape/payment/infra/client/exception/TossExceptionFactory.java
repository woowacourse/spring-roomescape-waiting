package roomescape.payment.infra.client.exception;

import org.springframework.http.HttpStatusCode;
import roomescape.payment.infra.client.dto.TossErrorResponse;
import roomescape.payment.infra.client.exception.TossBusinessException.AlreadyProcessed;
import roomescape.payment.infra.client.exception.TossBusinessException.CardRejected;
import roomescape.payment.infra.client.exception.TossBusinessException.DuplicatedOrder;
import roomescape.payment.infra.client.exception.TossBusinessException.GatewayConfig;
import roomescape.payment.infra.client.exception.TossBusinessException.InvalidRequest;
import roomescape.payment.infra.client.exception.TossBusinessException.PaymentNotFound;
import roomescape.payment.infra.client.exception.TossBusinessException.SessionExpired;
import roomescape.payment.infra.client.exception.TossInfrastructureException.Retryable;
import roomescape.payment.infra.client.exception.TossInfrastructureException.TossConnectionException;
import roomescape.payment.infra.client.exception.TossInfrastructureException.TossTimeoutException;

public class TossExceptionFactory {
    public static TossPaymentException create(HttpStatusCode status, TossErrorResponse error) {
        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> new AlreadyProcessed(error.message());
            case "DUPLICATED_ORDER_ID" -> new DuplicatedOrder(error.message());
            case "NOT_FOUND_PAYMENT_SESSION" -> new SessionExpired(error.message());
            case "INVALID_REQUEST" -> new InvalidRequest(error.message());
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> new GatewayConfig(error.message());
            case "REJECT_CARD_PAYMENT" -> new CardRejected(error.message());
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFound(error.message());
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new Retryable(error.message());
            case "TIMEOUT" -> new TossTimeoutException(error.message());
            case "CONNECTION_ERROR" -> new TossConnectionException(error.message());
            default -> new TossPaymentException(status, error.code(), error.message()) {};
        };
    }
}
