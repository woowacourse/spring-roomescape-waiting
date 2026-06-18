package roomescape.payment.client;

import org.springframework.http.HttpStatus;
import roomescape.exception.business.BusinessException;

public class PaymentTimeoutException extends BusinessException {

    public PaymentTimeoutException(String message) {
        super(HttpStatus.GATEWAY_TIMEOUT, message);
    }
}