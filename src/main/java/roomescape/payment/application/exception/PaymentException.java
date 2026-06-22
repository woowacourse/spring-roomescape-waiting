package roomescape.payment.application.exception;

import roomescape.global.RoomEscapeException;

public class PaymentException extends RoomEscapeException {

    private final String gatewayCode;

    public PaymentException(PaymentErrorCode errorCode) {
        this(errorCode, null);
    }

    public PaymentException(PaymentErrorCode errorCode, String gatewayCode) {
        super(errorCode);
        this.gatewayCode = gatewayCode;
    }

    public String gatewayCode() {
        return gatewayCode;
    }
}
