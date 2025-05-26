package roomescape.reservation.domain;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class WaitingOrder {
    private final AtomicInteger waitingOrderValue = new AtomicInteger(0);

    public void resetWaitingOrder() {
        waitingOrderValue.set(0);
    }

    public long issueNextWaitingOrder() {
        return waitingOrderValue.incrementAndGet();
    }
}
