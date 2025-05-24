package roomescape.application.reservation.event;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

@Component
@ActiveProfiles("test")
public class TestReservationCancelListener {

    private final AtomicBoolean called = new AtomicBoolean(false);

    @EventListener
    public void handle(ReservationCancelEvent event) {
        called.set(true);
    }

    public boolean isCalled() {
        return called.get();
    }

    public void reset() {
        called.set(false);
    }
}
