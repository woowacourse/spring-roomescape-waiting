package roomescape.payment.exception;

import roomescape.common.exception.RoomEscapeException;

/**
 * 나가는 호출이 자체 한도를 넘겨 외부로 보내지 않고 거부될 때 발생한다.
 */
public class OutboundRateLimitException extends RoomEscapeException {

    public OutboundRateLimitException() {
        super(PaymentErrorCode.PAYMENT_OUTBOUND_RATE_LIMITED);
    }
}
