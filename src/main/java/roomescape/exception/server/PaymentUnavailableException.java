package roomescape.exception.server;

import org.springframework.http.HttpStatus;
import roomescape.exception.base.RoomeScapeServerException;

public class PaymentUnavailableException extends RoomeScapeServerException {

    public PaymentUnavailableException() {
        super("결제 승인 결과를 확인할 수 없습니다. 잠시 후 다시 확인해 주세요.", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
