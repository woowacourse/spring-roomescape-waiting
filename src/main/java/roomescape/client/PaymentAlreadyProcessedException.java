package roomescape.client;

public class PaymentAlreadyProcessedException extends PaymentException {

    public PaymentAlreadyProcessedException(String message) {
        super("ALREADY_PROCESSED_PAYMENT", message);
    }
}
