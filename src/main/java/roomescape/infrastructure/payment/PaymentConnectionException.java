package roomescape.infrastructure.payment;

public class PaymentConnectionException extends RuntimeException {

    public PaymentConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}