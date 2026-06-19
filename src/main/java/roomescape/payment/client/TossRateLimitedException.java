package roomescape.payment.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import roomescape.payment.PaymentGatewayException;

public class TossRateLimitedException extends PaymentGatewayException {

    public TossRateLimitedException() {
        super("토스 결제 서버가 일시적으로 요청을 제한하고 있습니다. 잠시 후 다시 시도해 주세요.");
    }

    @Override
    public HttpStatusCode getStatus() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }

    @Override
    public String getCode() {
        return "TOSS_RATE_LIMITED";
    }
}