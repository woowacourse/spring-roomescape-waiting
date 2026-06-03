package roomescape.domain.waiting;

import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;
import roomescape.infra.queue.AsyncQueue;

@Component
public class WaitingQueue extends AsyncQueue<WaitingRequest, WaitingResponse> {

    @Override
    protected String toJobId(WaitingRequest request) {
        return request.date() + ":" + request.timeId() + ":" + request.themeId() + ":" + request.name();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void evictExpiredResults() {
        evictResultsBefore(LocalDate.now());
    }
}
