package roomescape.global.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Scheduler {
    public static final int SINGLE_POOL_SIZE = 1;

    private final ScheduledExecutorService scheduler;

    public Scheduler() {
        this.scheduler = Executors.newScheduledThreadPool(SINGLE_POOL_SIZE);
    }

    public void executeAfterDelay(Runnable command, long delay, TimeUnit timeUnit) {
        scheduler.schedule(command, delay, timeUnit);
    }
}
