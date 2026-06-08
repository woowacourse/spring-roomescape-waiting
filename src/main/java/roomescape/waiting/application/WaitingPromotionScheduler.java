package roomescape.waiting.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WaitingPromotionScheduler {

    private final WaitingService waitingService;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void promoteWaitingWithoutReservation() {
        waitingService.promoteWaitingWithoutReservation();
    }
}
