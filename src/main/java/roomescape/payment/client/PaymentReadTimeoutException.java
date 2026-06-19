package roomescape.payment.client;

public class PaymentReadTimeoutException extends RuntimeException {
    public PaymentReadTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}