package roomescape.payment.client;

public class PaymentNetworkException extends RuntimeException {
    public PaymentNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}