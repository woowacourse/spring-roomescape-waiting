package roomescape.payment.client;

import org.springframework.http.HttpStatus;
import roomescape.exception.business.BusinessException;

public class PaymentConnectionException extends BusinessException {

    public PaymentConnectionException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}