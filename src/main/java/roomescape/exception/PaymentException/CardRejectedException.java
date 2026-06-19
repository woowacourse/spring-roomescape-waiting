package roomescape.exception.PaymentException;

public class CardRejectedException extends RuntimeException {
    public CardRejectedException(String message) {
        super(message);
    }
}
