package roomescape.payment;

public class PaymentGatewayConnectionException extends RuntimeException {

    public PaymentGatewayConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
