package roomescape.payment.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.BusinessException;

public abstract class PaymentFailureException extends BusinessException {

    private final String code;

    protected PaymentFailureException(HttpStatus status, String code, String message) {
        super(status, message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
