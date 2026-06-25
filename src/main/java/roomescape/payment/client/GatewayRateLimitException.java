package roomescape.payment.client;

import roomescape.service.payment.PaymentFailureCategory;
import roomescape.service.payment.PaymentGatewayException;

/**
 * 토스가 429 를 반복해 재시도 횟수를 모두 소진했을 때 던지는 예외.
 *
 * <p>429 는 "아직 처리되지 않음"을 뜻하므로 결제는 그대로 두고(상태 유지) 안전하게 다시 시도할 수 있다.
 */
public class GatewayRateLimitException extends PaymentGatewayException {

    public static final String CODE = "TOO_MANY_REQUESTS";

    public GatewayRateLimitException(String message) {
        super(PaymentFailureCategory.RATE_LIMITED, CODE, message);
    }
}
