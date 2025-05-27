package roomescape.reservation.domain;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class WaitingOrder {
    private static final AtomicInteger WAITING_ORDER_VALUE = new AtomicInteger(0);

    public void resetWaitingOrder() {
        WAITING_ORDER_VALUE.set(0);
    }

    public long issueNextWaitingOrder() {
        return WAITING_ORDER_VALUE.incrementAndGet();
    }
}
