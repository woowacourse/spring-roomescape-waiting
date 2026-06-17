package roomescape.client;

public class PaymentGatewayRetryableException extends PaymentException {

    public PaymentGatewayRetryableException(String code, String message) {
        super(code, message);
    }
}
