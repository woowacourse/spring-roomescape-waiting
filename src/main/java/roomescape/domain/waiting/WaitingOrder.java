package roomescape.domain.waiting;

import jakarta.persistence.Embeddable;

@Embeddable
public class WaitingOrder {

    private int waitingOrder;

    protected WaitingOrder() {
    }

    public WaitingOrder(int waitingOrder) {
        validateWaitingOrder(waitingOrder);
        this.waitingOrder = waitingOrder;
    }

    public void decreaseWaitingOrderByOne() {
        if (waitingOrder > 1) {
            waitingOrder--;
        }
    }

    public boolean isFirstOrder() {
        return waitingOrder == 1;
    }

    private void validateWaitingOrder(int order) {
        if (order < 1) {
            throw new IllegalArgumentException(
                    "[ERROR] 잘못된 대기 순서입니다. 관리자에게 문의해주세요.",
                    new Throwable("order : " + order));
        }
    }

    public int getWaitingOrder() {
        return waitingOrder;
    }
}
