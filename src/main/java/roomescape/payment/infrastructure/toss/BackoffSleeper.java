package roomescape.payment.infrastructure.toss;

import java.time.Duration;

@FunctionalInterface
interface BackoffSleeper {

    void sleep(Duration duration) throws InterruptedException;
}
