package roomescape.payment;

public class PaymentResultUnknownException extends RuntimeException {

    public PaymentResultUnknownException(String message, Throwable cause) {
        super(message, cause);
    }
}
