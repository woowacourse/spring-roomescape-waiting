package roomescape.ratelimit;

import java.time.Duration;

@FunctionalInterface
interface Sleeper {

    void sleep(Duration duration) throws InterruptedException;
}
