package roomescape.global.exception;

public class RetryablePaymentGatewayException extends RuntimeException {

    public RetryablePaymentGatewayException() {
        super("결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.");
    }

    public RetryablePaymentGatewayException(Throwable cause) {
        super("결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.", cause);
    }
}
