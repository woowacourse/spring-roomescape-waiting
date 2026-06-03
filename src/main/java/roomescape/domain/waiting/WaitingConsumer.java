package roomescape.domain.waiting;

import org.springframework.stereotype.Component;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;
import roomescape.exception.RoomescapeException;
import roomescape.infra.queue.AsyncConsumer;
import roomescape.infra.queue.JobResult;

@Component
public class WaitingConsumer extends AsyncConsumer<WaitingRequest, WaitingResponse> {

    private final WaitingService waitingService;

    public WaitingConsumer(WaitingQueue waitingQueue, WaitingService waitingService) {
        super(waitingQueue);
        this.waitingService = waitingService;
    }

    @Override
    protected String threadName() {
        return "waiting-consumer";
    }

    @Override
    protected JobResult<WaitingResponse> process(WaitingRequest request) {
        try {
            WaitingResponse response = waitingService.createWaiting(request);
            return JobResult.success(response);
        } catch (RoomescapeException e) {
            return JobResult.failed(e.getMessage());
        }
    }
}
