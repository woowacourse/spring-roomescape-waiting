package roomescape.global.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Scheduler {
    private static final int POOL_SIZE = 1;

    private final ScheduledExecutorService scheduler;

    public Scheduler() {
        this.scheduler = Executors.newScheduledThreadPool(POOL_SIZE);
    }

    public void executeAfterDelay(Runnable command, long delay, TimeUnit timeUnit) {
        scheduler.schedule(command, delay, timeUnit);
    }
}
