package roomescape.exception;

public class PaymentConfirmationUnknownException extends RoomescapeException {

    private static final String ERROR_CODE = "PAYMENT_CONFIRMATION_UNKNOWN";
    private static final String MESSAGE = "결제 승인 여부를 확인할 수 없습니다. 잠시 후 다시 확인해 주세요.";

    public PaymentConfirmationUnknownException() {
        super(ERROR_CODE, MESSAGE);
    }

    public PaymentConfirmationUnknownException(Throwable cause) {
        super(ERROR_CODE, MESSAGE, cause);
    }
}
