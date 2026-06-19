package roomescape.ratelimit;

import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class ThreadBackoffSleeper implements BackoffSleeper {

    @Override
    public void sleep(Duration duration) {
        if (duration.isZero() || duration.isNegative()) {
            return;
        }
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry", e);
        }
    }
}
