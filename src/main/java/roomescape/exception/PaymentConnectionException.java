package roomescape.exception;

public class PaymentConnectionException extends RoomescapeException {

    private static final String ERROR_CODE = "PAYMENT_CONNECTION_FAILED";
    private static final String MESSAGE = "결제 승인 요청을 완료하지 못했습니다. 다시 시도해 주세요.";

    public PaymentConnectionException(Throwable cause) {
        super(ERROR_CODE, MESSAGE, cause);
    }
}
