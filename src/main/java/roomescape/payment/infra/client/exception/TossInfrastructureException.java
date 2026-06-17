package roomescape.payment.infra.client.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public abstract class TossInfrastructureException extends TossPaymentException {
    protected TossInfrastructureException(HttpStatusCode status, String code, String message) {
        super(status, code, message);
    }

    /**
     * 500 - 토스 내부 시스템 오류. 재시도 대상.
     */
    public static class Retryable extends TossInfrastructureException {

        public Retryable(String message) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", message);
        }

    }

    public static class TossTimeoutException extends TossInfrastructureException {

        public TossTimeoutException(String message) {
            super(HttpStatus.REQUEST_TIMEOUT, "TIMEOUT", message);
        }
    }

    public static class TossConnectionException extends TossInfrastructureException {
        public TossConnectionException(String message) {
            super(HttpStatus.SERVICE_UNAVAILABLE, "CONNECTION_ERROR", message);
        }
    }
}
