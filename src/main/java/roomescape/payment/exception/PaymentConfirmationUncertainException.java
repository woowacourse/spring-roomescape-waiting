package roomescape.payment.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.BusinessException;

public class PaymentConfirmationUncertainException extends BusinessException {

    public PaymentConfirmationUncertainException(String orderId) {
        super(HttpStatus.GATEWAY_TIMEOUT,
                "결제 승인 응답을 받지 못했습니다. 이미 승인됐을 수 있어 확인이 필요합니다. orderId=" + orderId);
    }
}
