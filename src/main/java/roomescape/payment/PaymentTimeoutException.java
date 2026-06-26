package roomescape.payment;

public class PaymentTimeoutException extends RuntimeException {

    public PaymentTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
