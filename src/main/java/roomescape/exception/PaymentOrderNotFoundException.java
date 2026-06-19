package roomescape.exception;

public class PaymentOrderNotFoundException extends RoomescapeBaseException {

    public PaymentOrderNotFoundException(String orderId) {
        super("결제 주문을 찾을 수 없습니다. orderId=" + orderId);
    }
}
