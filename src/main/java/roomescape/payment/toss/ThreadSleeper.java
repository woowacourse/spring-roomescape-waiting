package roomescape.payment.toss;

import java.time.Duration;

public class ThreadSleeper implements Sleeper {

    @Override
    public void sleep(Duration duration) {
        long millis = duration.toMillis();
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("재시도 대기 중 인터럽트되었습니다.", e);
        }
    }
}
