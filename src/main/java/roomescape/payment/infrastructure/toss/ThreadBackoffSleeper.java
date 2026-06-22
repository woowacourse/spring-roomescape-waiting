package roomescape.payment.infrastructure.toss;

import java.time.Duration;

class ThreadBackoffSleeper implements BackoffSleeper {

    @Override
    public void sleep(final Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }
}
