package roomescape.domain.waiting;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;

@Component
public class WaitingQueue {

    private final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();
    private final WaitingService waitingService;

    public WaitingQueue(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    public WaitingResponse submit(WaitingRequest request) {
        Object lock = locks.computeIfAbsent(slotKey(request), k -> new Object());
        synchronized (lock) {
            return waitingService.createWaiting(request);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void evictExpiredLocks() {
        LocalDate today = LocalDate.now();
        locks.keySet().removeIf(key -> LocalDate.parse(key.split(":")[0]).isBefore(today));
    }

    private String slotKey(WaitingRequest request) {
        return request.date() + ":" + request.timeId() + ":" + request.themeId();
    }
}