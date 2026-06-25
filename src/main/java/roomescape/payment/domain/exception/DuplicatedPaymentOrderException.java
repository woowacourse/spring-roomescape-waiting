package roomescape.payment.domain.exception;

import roomescape.common.exception.BusinessException;

public class DuplicatedPaymentOrderException extends BusinessException {

    public DuplicatedPaymentOrderException() {
        super(PaymentErrorType.DUPLICATED_ORDER);
    }
}
