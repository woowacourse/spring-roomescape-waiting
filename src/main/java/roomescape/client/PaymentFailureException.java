package roomescape.client;

public class PaymentFailureException extends PaymentException {

    public PaymentFailureException(String code, String message) {
        super(code, message);
    }
}
