package roomescape.infrastructure.payment;

public class PaymentUnknownException extends RuntimeException {

    public PaymentUnknownException(String message, Throwable cause) {
        super(message, cause);
    }
}