package roomescape.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import roomescape.waiting.service.WaitingHistoryService;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private final WaitingHistoryService waitingHistoryService;

    public SchedulerConfig(WaitingHistoryService waitingHistoryService) {
        this.waitingHistoryService = waitingHistoryService;
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void run() {
        waitingHistoryService.approveReservationStatus();
    }
}
