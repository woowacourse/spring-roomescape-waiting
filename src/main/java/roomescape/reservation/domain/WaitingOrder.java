package roomescape.reservation.domain;

import java.util.concurrent.atomic.AtomicInteger;

public class WaitingOrder {

    private final AtomicInteger waitingOrderValue = new AtomicInteger(0);

    public long issueNextWaitingOrder() {
        return waitingOrderValue.incrementAndGet();
    }
}
