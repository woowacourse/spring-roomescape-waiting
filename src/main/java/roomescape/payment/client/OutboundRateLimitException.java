package roomescape.payment.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import roomescape.payment.PaymentGatewayException;

public class OutboundRateLimitException extends PaymentGatewayException {

    public OutboundRateLimitException() {
        super("결제 서버 호출 한도에 도달했습니다. 잠시 후 다시 시도해 주세요.");
    }

    @Override
    public HttpStatusCode getStatus() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }

    @Override
    public String getCode() {
        return "OUTBOUND_RATE_LIMITED";
    }
}
