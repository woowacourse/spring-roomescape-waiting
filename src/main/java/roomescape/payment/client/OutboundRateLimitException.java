package roomescape.payment.client;

import roomescape.service.payment.PaymentFailureCategory;
import roomescape.service.payment.PaymentGatewayException;

/**
 * 나가는 결제 호출이 자체 Rate Limit 을 초과해 외부로 보내지 않고 거부할 때 던지는 예외.
 *
 * <p>호출이 토스에 도달하지 않았으므로 결제는 그대로 두고(상태 유지) 안전하게 다시 시도할 수 있다.
 */
public class OutboundRateLimitException extends PaymentGatewayException {

    public static final String CODE = "OUTBOUND_RATE_LIMITED";

    public OutboundRateLimitException(String message) {
        super(PaymentFailureCategory.RATE_LIMITED, CODE, message);
    }
}
