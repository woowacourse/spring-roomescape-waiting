package roomescape.domain.waiting;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.waiting.dto.MyWaitingsResponse;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;
import roomescape.infra.queue.JobResult;

@RestController
public class WaitingController {

    private final WaitingQueue waitingQueue;
    private final WaitingService waitingService;

    public WaitingController(WaitingQueue waitingQueue, WaitingService waitingService) {
        this.waitingQueue = waitingQueue;
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations/waiting")
    public ResponseEntity<Map<String, String>> createWaiting(
            @Valid @RequestBody WaitingRequest waitingRequest
    ) {
        String jobId = waitingQueue.enqueue(waitingRequest);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @GetMapping("/reservations/waiting/status/{jobId}")
    public ResponseEntity<JobResult<WaitingResponse>> getJobStatus(@PathVariable String jobId) {
        JobResult<WaitingResponse> result = waitingQueue.getResult(jobId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reservations/waiting/mine")
    public ResponseEntity<MyWaitingsResponse> getMyWaitings(
            @RequestParam String name
    ) {
        MyWaitingsResponse response = waitingService.getMyWaitings(name);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reservations/waiting/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id
    ) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
