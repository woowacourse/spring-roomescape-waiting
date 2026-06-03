package roomescape.domain.waiting;

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
}
