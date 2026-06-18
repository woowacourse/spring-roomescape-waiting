package roomescape.ratelimit;

import java.time.Duration;

@FunctionalInterface
public interface Sleeper {

    void sleep(Duration duration) throws InterruptedException;
}
