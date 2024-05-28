package roomescape.domain.waiting;

import jakarta.persistence.Embeddable;

@Embeddable
public class WaitingOrder {

    private static final int MIN_ORDER = 1;

    private int waitingOrder;

    protected WaitingOrder() {
    }

    public WaitingOrder(int waitingOrder) {
        validateWaitingOrder(waitingOrder);
        this.waitingOrder = waitingOrder;
    }

    public void decreaseWaitingOrderByOne() {
        if (waitingOrder > MIN_ORDER) {
            waitingOrder--;
        }
    }

    public boolean isFirstOrder() {
        return waitingOrder == MIN_ORDER;
    }

    public boolean isWaitingOrderGreaterThan(int waitingOrderToCompare) {
        return waitingOrder > waitingOrderToCompare;
    }

    private void validateWaitingOrder(int order) {
        if (order < MIN_ORDER) {
            throw new IllegalArgumentException(
                    "[ERROR] 잘못된 대기 순서입니다. 관리자에게 문의해주세요.",
                    new Throwable("order : " + order));
        }
    }

    public int getWaitingOrder() {
        return waitingOrder;
    }
}
