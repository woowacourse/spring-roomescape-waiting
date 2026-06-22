package roomescape.exception.server;

import org.springframework.http.HttpStatus;
import roomescape.exception.base.RoomeScapeServerException;

public class OutboundRateLimitException extends RoomeScapeServerException {

    public OutboundRateLimitException() {
        super("외부 결제 승인 요청이 일시적으로 제한되었습니다. 잠시 후 다시 시도해 주세요.", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
