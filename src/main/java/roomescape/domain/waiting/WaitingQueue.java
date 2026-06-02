package roomescape.domain.waiting;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Component;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResult;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@Component
public class WaitingQueue {

    private final BlockingQueue<WaitingMessage> queue = new LinkedBlockingQueue<>();
    private final ConcurrentMap<String, WaitingResult> results = new ConcurrentHashMap<>();

    public String enqueue(WaitingRequest request) {
        String jobId = toJobId(request);
        results.put(jobId, WaitingResult.pending());
        try {
            queue.add(new WaitingMessage(jobId, request));
        } catch (IllegalStateException e) {
            throw new RoomescapeException(ErrorCode.SERVER_OVERLOADED);
        }
        return jobId;
    }

    private String toJobId(WaitingRequest request) {
        return request.date() + ":" + request.timeId() + ":" + request.themeId() + ":" + request.name();
    }

    public WaitingMessage take() throws InterruptedException {
        return queue.take();
    }

    public void storeResult(String jobId, WaitingResult result) {
        results.put(jobId, result);
    }

    public WaitingResult getResult(String jobId) {
        return results.get(jobId);
    }
}