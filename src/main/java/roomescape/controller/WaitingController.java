package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.annotation.AuthenticationPrincipal;
import roomescape.controller.request.WaitingRequest;
import roomescape.model.Member;
import roomescape.model.Waiting;
import roomescape.service.WaitingService;

import java.net.URI;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waiting")
    public ResponseEntity<Waiting> createWaiting(@RequestBody WaitingRequest request,
                                                 @AuthenticationPrincipal Member member) {
        Waiting waiting = waitingService.addWaiting(request, member);
        return ResponseEntity.created(URI.create("/waiting/" + waiting.getId())).body(waiting);
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable("id") long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
