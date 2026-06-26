package roomescape.payment.toss;

import java.time.Duration;

@FunctionalInterface
public interface Sleeper {

    void sleep(Duration duration);
}
