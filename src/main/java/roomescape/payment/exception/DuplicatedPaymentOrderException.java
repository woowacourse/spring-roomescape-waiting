package roomescape.payment.exception;

import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorType;

public class DuplicatedPaymentOrderException extends BusinessException {

    public DuplicatedPaymentOrderException() {
        super(ErrorType.PAYMENT_DUPLICATED_ORDER);
    }
}
