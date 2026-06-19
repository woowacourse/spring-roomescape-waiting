package roomescape.client.ratelimit;

import roomescape.client.PaymentGatewayRetryableException;

public class OutboundRateLimitException extends PaymentGatewayRetryableException {

    public OutboundRateLimitException(String message) {
        super("OUTBOUND_RATE_LIMIT_EXCEEDED", message);
    }
}
