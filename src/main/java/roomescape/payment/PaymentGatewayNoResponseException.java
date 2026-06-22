package roomescape.payment;

public class PaymentGatewayNoResponseException extends RuntimeException {

    public PaymentGatewayNoResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
