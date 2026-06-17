package roomescape.client;

public class PaymentGatewayConfigurationException extends PaymentException {

    public PaymentGatewayConfigurationException(String code, String message) {
        super(code, message);
    }
}
