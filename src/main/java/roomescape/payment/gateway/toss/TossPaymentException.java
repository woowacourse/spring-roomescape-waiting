package roomescape.payment.gateway.toss;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import roomescape.payment.gateway.toss.dto.TossErrorResponse;

public class TossPaymentException extends RuntimeException {

    private final HttpStatusCode status;
    private final String code;

    public TossPaymentException(HttpStatusCode status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static TossPaymentException of(HttpStatusCode status, TossErrorResponse error) {
        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> new AlreadyProcessed(error.message());
            case "DUPLICATED_ORDER_ID" -> new DuplicatedOrder(error.message());
            case "NOT_FOUND_PAYMENT_SESSION" -> new SessionExpired(error.message());
            case "INVALID_REQUEST" -> new InvalidRequest(error.message());
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> new GatewayConfig(error.message());
            case "REJECT_CARD_PAYMENT" -> new CardRejected(error.message());
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFound(error.message());
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new Retryable(error.message());
            default -> new TossPaymentException(status, error.code(), error.message());
        };
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public static class AlreadyProcessed extends TossPaymentException {
        public AlreadyProcessed(String message) {
            super(HttpStatus.BAD_REQUEST, "ALREADY_PROCESSED_PAYMENT", message);
        }
    }

    public static class DuplicatedOrder extends TossPaymentException {
        public DuplicatedOrder(String message) {
            super(HttpStatus.BAD_REQUEST, "DUPLICATED_ORDER_ID", message);
        }
    }

    public static class SessionExpired extends TossPaymentException {
        public SessionExpired(String message) {
            super(HttpStatus.BAD_REQUEST, "NOT_FOUND_PAYMENT_SESSION", message);
        }
    }

    public static class InvalidRequest extends TossPaymentException {
        public InvalidRequest(String message) {
            super(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
        }
    }

    /** 인증 실패 — 키 설정 오류, 운영 알림 대상 */
    public static class GatewayConfig extends TossPaymentException {
        public GatewayConfig(String message) {
            super(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_KEY", message);
        }
    }

    public static class CardRejected extends TossPaymentException {
        public CardRejected(String message) {
            super(HttpStatus.FORBIDDEN, "REJECT_CARD_PAYMENT", message);
        }
    }

    public static class PaymentNotFound extends TossPaymentException {
        public PaymentNotFound(String message) {
            super(HttpStatus.NOT_FOUND, "NOT_FOUND_PAYMENT", message);
        }
    }

    /** Toss 내부 오류 — 재시도 대상 */
    public static class Retryable extends TossPaymentException {
        public Retryable(String message) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", message);
        }
    }
}
