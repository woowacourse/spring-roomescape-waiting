package roomescape.domain.waiting;

import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;
import roomescape.exception.RoomescapeException;
import roomescape.infra.queue.AsyncQueue;
import roomescape.infra.queue.JobResult;

@Component
public class WaitingQueue extends AsyncQueue<WaitingRequest, WaitingResponse> {

    private final WaitingService waitingService;

    public WaitingQueue(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @Override
    protected String toSlotId(WaitingRequest request) {
        return request.date() + ":" + request.timeId() + ":" + request.themeId();
    }

    @Override
    protected String toJobId(WaitingRequest request) {
        return request.date() + ":" + request.timeId() + ":" + request.themeId() + ":" + request.name();
    }

    @Override
    protected JobResult<WaitingResponse> process(WaitingRequest request) {
        try {
            return JobResult.success(waitingService.createWaiting(request));
        } catch (RoomescapeException e) {
            return JobResult.failed(e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void evictExpiredResults() {
        evictBefore(LocalDate.now());
    }
}