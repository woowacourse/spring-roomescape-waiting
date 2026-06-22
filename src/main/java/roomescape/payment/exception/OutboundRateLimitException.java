package roomescape.payment.exception;

public class OutboundRateLimitException extends PaymentCommunicationException {

    public OutboundRateLimitException(final String message) {
        super(message, null);
    }
}
