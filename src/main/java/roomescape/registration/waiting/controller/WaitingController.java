package roomescape.registration.waiting.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.registration.waiting.dto.WaitingRequest;
import roomescape.registration.waiting.dto.WaitingResponse;
import roomescape.registration.waiting.service.WaitingService;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> waitingSave(@RequestBody WaitingRequest waitingRequest,
                                                         @LoginMemberId long memberId) {
        WaitingResponse waiting = waitingService.addWaiting(waitingRequest, memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(waiting);
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponse>> waitingList() {
        List<WaitingResponse> waitings = waitingService.findWaitings();

        return ResponseEntity.ok(waitings);
    }

    @DeleteMapping("/waitings/{waitingId}")
    public ResponseEntity<WaitingResponse> waitingRemove(@PathVariable long waitingId) {
        waitingService.removeWaiting(waitingId);
        return ResponseEntity.noContent().build();
    }
}
