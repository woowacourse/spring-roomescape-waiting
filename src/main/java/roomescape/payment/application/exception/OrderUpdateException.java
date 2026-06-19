package roomescape.payment.application.exception;

import roomescape.common.exception.UpdateException;

public class OrderUpdateException extends UpdateException {

    public OrderUpdateException(String message) {
        super(message);
    }

    public OrderUpdateException(String message, Exception e) {
        super(message, e);
    }
}
