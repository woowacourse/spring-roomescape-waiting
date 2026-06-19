package roomescape.ratelimit;

import java.time.Duration;

@FunctionalInterface
public interface BackoffSleeper {

    void sleep(Duration duration);
}
