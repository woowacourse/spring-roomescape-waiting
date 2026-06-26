package roomescape.infrastructure.toss;

import java.time.Duration;

@FunctionalInterface
public interface BackOffSleeper {
    void sleep(Duration duration) throws InterruptedException;
}
