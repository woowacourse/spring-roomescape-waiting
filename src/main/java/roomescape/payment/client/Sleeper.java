package roomescape.payment.client;

import java.io.IOException;
import java.time.Duration;

@FunctionalInterface
interface Sleeper {

    void sleep(Duration duration) throws IOException;

    static Sleeper threadSleep() {
        return duration -> {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting to retry Toss API", e);
            }
        };
    }
}